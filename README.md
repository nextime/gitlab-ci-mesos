# GitLab CI Mesos Framework

[![Build Status](https://travis-ci.org/deric/gitlab-ci-mesos.png?branch=master)](https://travis-ci.org/deric/gitlab-ci-mesos)

Mesos framework for running Gitlab CI tasks.

![gitlab-ci-scheme](https://raw.github.com/deric/gitlab-ci-mesos/diagram/diagrams/gitlab-ci.png)

Currently this project contains a GitLab scheduler (assign jobs to slaves) and GitLab executor (execute job and send updates to CI API).

Project contains currently two compoments, first one is a scheduler, which is a Mesos framework that checks for new builds from a GitLab CI each time it gets new resource offers. The other one is an executor that sends updates to GitLab CI. This executor could be replaced by your own code (in future it might be a separate project).

Executor script must be on each slave's PATH.

## Requirements

  * GitLab >= 6.4
  * GitLab CI ~ 4.2
  * Mesos
  * Oracle Java 7

## Development / Building

from project root (requires Maven installed, Maven 3 is fine, Maven 2 should also work)

```bash
 $ mvn install
```

## Deployment

### Debian

You can build a package for Debian simply with

```bash
 $ ./build_package.sh --version 0.0.2
```

Set variables in `/etc/gitlab-ci/scheduler.conf`:

  * `MESOS_MASTER` -- Zookeeper URL or IP address of the master
  * `GITLAB_CI_URL` -- e.g. `http://my.gitlab.org`
  * `GITLAB_CI_TOKEN` -- token for registration of new runner (you'll find it in your Gitlab CI)




