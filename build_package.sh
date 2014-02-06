#!/bin/bash
#
#   $ ./build_package
#
set -o errexit -o nounset -o pipefail
export LC_ALL=C
function -h {
cat <<USAGE
GitLab-CI-Mesos building script

USAGE
}; function --help { -h ;}

this="$(cd "$(dirname "$0")" && pwd -P)"
name="gitlab-ci-mesos"
version="${version:-9999}"

function main {
  while [[ $# -gt 0 ]]
  do
    case "$1" in                                      # Munging globals, beware
      --repo)                   repo="$2"    ; shift 2 ;;
      --version)                version="$2" ; shift 2 ;;
      *)                        err 'Argument error. Please see help.' ;;
    esac
  done
  go
}

use_git_version=true
function maybe_append_git_hash {
  if $use_git_version && git rev-parse --git-dir &>/dev/null
  then out "$1-g$(git log -n1 --format=%h)"
  else out "$1"
  fi
}

function go {
  dependencies
  cleanup
  build
  create_installation
  pkg
}

function lsb_release_tag {
  lsb_release --id --release | cut -d: -f2 | tr A-Z a-z | xargs | tr ' ' '/'
}

function get_codename {
  case "$(lsb_release_tag)" in
    ubuntu/*|debian/*)
      lsb_release -c | cut -d: -f2 | tr A-Z a-z | xargs | tr ' ' '/'
    ;;
    centos/*|redhat/*)
      err "not implemented yet"
    ;;
    *)                 err "Not sure how to configure logrotate for: $linux" ;;
  esac
}

function architecture {
  case "$(lsb_release_tag)" in
    ubuntu/*|debian/*) dpkg-architecture -qDEB_BUILD_ARCH ;;
    centos/*|redhat/*) arch ;;
    *)                 err "Not sure how to determine arch for: $linux" ;;
  esac
}

function get_system_info {
  linux="$(os_release)"                 # <distro>/<version>, like ubuntu/12.10
  arch="$(architecture)"          # In the format used to label distro packages
  gem_bin="$(find_gem_bin)"                          # Might not be on the PATH
  codename="$(get_codename)"
}

function find_gem_bin {
  gem env | sed -n '/^ *- EXECUTABLE DIRECTORY: */ { s/// ; p }'
}

function url_fragment {
  local step1="${1%#}"#       # Ensure URL ends in #, even if it has a fragment
  local step2="${step1#*#}"                                # Clip up to first #
  out "${step2%#}"                    # Remove trailing #, guaranteed by step 1
}

# Split URL in to resource, query and fragment.
function url_split {
  local fragment= query=
  local sans_fragment="${1%%#*}"
  local sans_query="${sans_fragment%%'?'*}"
  [[ $1             = $sans_fragment ]] || fragment="${1#*#}"
  [[ $sans_fragment = $sans_query    ]] || query="${sans_fragment#*'?'}"
  out "$sans_query"
  out "$query"
  out "$fragment"
}

function dependencies {
  #check dependencies
  DEPENDENCIES=(maven)
  i=0
  expstatus="Status: install ok installed"
  for package in ${DEPENDENCIES[@]}
  do
    status=`dpkg -s ${package} | grep Status:`
   if [[ "${status}" != *"${expstatus}"* ]]; then
     err "missing package: ${package}"
     i=$((i+1))
   fi
  done
  if [[ i -gt 0 ]]; then
    echo "please install missing dependencies"
    exit 1
  fi
}

function cleanup {
  if [ -e "$this"/*.deb ];then
    msg "removing old deb package"
    rm "$this"/*.deb
  fi
  if [ -d "$this"/deb ]; then
    msg "removing old deb directory"
    rm -rf "$this"/deb
  fi
  # remove all old jars, we want exactly one
  if [ -d "$this/target" ]; then
    find "$this/target" -type f -name *.jar -exec rm -f {} \;
  fi
}

function build {
  FWDIR="$(cd `dirname $0`; pwd)"

  mvn assembly:assembly
}

function create_installation {(
  mkdir -p "${this}/deb"
  cd "${this}/deb"
  mkdir -p usr/share/gitlab-ci/bin
  mkdir -p etc/gitlab-ci
  mkdir -p usr/local/bin
  mkdir -p var/log/gitlab-ci
  # framework dir
  FWDIR="${this}"
  DEBDIR="${this}/deb"
  DISTDIR="${this}/deb/usr/share/gitlab-ci"

  cp $FWDIR/scripts/scheduler.conf "$DEBDIR/etc/gitlab-ci/scheduler.conf"
  cp $FWDIR/scripts/exec.properties "$DISTDIR/"
  cp $FWDIR/scripts/scheduler.properties "$DISTDIR/"

  # Copy JAR
  cp $FWDIR/target/*-jar-with-dependencies.jar "$DISTDIR/"

  cp $FWDIR/scripts/gitlab-ci-scheduler "$DISTDIR/bin/"
  cp $FWDIR/scripts/gitlab-ci-exec "$DEBDIR/usr/local/bin"

  init_scripts "$linux"
  logrotate "$linux"
)}

function init_scripts {
  case "$1" in
    debian/*) mkdir -p etc/init.d
              cp -p "$this"/scripts/debian.init "${DEBDIR}/etc/init.d/gitlab-ci" ;;
    ubuntu/*) mkdir -p etc/init
              cp "$this"/scripts/ubuntu.upstart "${DEBDIR}/etc/init/gitlab-ci.conf" ;;
    *) err "Not sure how to make init scripts for: $1" ;;
  esac
}

function logrotate {
  case "$linux" in
    ubuntu/*|debian/*)
      mkdir -p etc/logrotate.d
      cp "$this"/scripts/logrotate "${DEBDIR}/"etc/logrotate.d/gitlab-ci
    ;;
    centos/*|redhat/*)
      err "not implemented yet"
    ;;
    *)                 err "Not sure how to configure logrotate for: $linux" ;;
  esac
}

function pkg {
  echo "creating package..."
  local scripts="${linux%%/*}"
  local opts=( -t deb
               -d 'java7-runtime-headless | java6-runtime-headless'
  )
  fpm_ "${opts[@]}" -p "$this"/"$name-$version.deb"

}

function fpm_ {
  local version="$(maybe_append_git_hash "$version")"
  local opts=( -s dir
               -n "$name"
               -v "$version"
               --description "Mesos scheduler takes tasks from GitLab CI API and assign them to Mesos slaves where tasks are executed."
               --url=""
               -a "$arch"
               --license "Apache 2.0"
               --category misc
               --vendor "FIT CVUT"
               -m tomas.barton@fit.cvut.cz
               --after-install "$this/scripts/postinst.sh"
               --prefix=/ )
  ( cd "${this}/deb" && "$gem_bin"/fpm "${opts[@]}" "$@" -- . )
}

function os_release {
  msg "Trying /etc/os-release..."
  if [[ -f /etc/os-release ]]
  then
    ( source /etc/os-release && display_version "$ID" "$VERSION_ID" )
    return 0
  fi
  msg "Trying /etc/redhat-release..."
  if [[ -f /etc/redhat-release ]]
  then
    # Seems to be formatted as: <distro> release <version> (<remark>)
    #                           CentOS release 6.3 (Final)
    if [[ $(cat /etc/redhat-release) =~ \
          ^(.+)' '+release' '+([^ ]+)' '+'('[^')']+')'$ ]]
    then
      local os
      case "${BASH_REMATCH[1]}" in
        'Red Hat '*) os=RedHat ;;
        *)           os="${BASH_REMATCH[1]}" ;;
      esac
      display_version "$os" "${BASH_REMATCH[2]}"
      return 0
    else
      err "/etc/redhat-release not like: <distro> release <version> (<remark>)"
    fi
  fi
  if which sw_vers &> /dev/null
  then
    local product="$(sw_vers -productName)"
    case "$product" in
      'Mac OS X') display_version MacOSX "$(sw_vers -productVersion)" ;;
      *) err "Expecting productName to be 'Mac OS X', not '$product'!";;
    esac
    return 0
  fi
  err "Could not determine OS version!"
}

function display_version {
  local os="$( tr A-Z a-z <<<"$1" )" version="$( tr A-Z a-z <<<"$2" )"
  case "$os" in
    redhat|centos|debian) out "$os/${version%%.*}" ;;   # Ignore minor versions
    macosx)               out "$os/${version%.*}" ;;  # Ignore bug fix releases
    *)                    out "$os/$version" ;;
  esac
}


function msg { out "$*" >&2 ;}
function err { local x=$? ; msg "$*" ; return $(( $x == 0 ? 1 : $x )) ;}
function out { printf '%s\n' "$*" ;}

if [[ ${1:-} ]] && declare -F | cut -d' ' -f3 | fgrep -qx -- "${1:-}"
then
  case "$1" in
    -h|--help|go|url_split|create_installation|build|osx_) : ;;
    *) get_system_info ;;
  esac
  "$@"
else
  get_system_info
  main "$@"
fi

