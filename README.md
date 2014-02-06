# GitLab CI Mesos Framework

Mesos framework for running Gitlab CI tasks. 

![gitlab-ci-scheme](https://raw.github.com/deric/gitlab-ci-mesos/diagram/diagrams/gitlab-ci.png)

Currently this project contains a GitLab scheduler (assign jobs to slaves) and GitLab executor (execute job and send updates to CI API).

## Development / Building

from project root (requires Maven installed, Maven 3 is fine, Maven 2 should also work)

   mvn install

   
## Deployment

You can build a package for Debian simply with

   $ ./build_package.sh --version 0.0.2





