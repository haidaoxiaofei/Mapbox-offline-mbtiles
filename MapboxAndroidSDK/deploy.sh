#!/usr/bin/env bash
gradle -b deploy.gradle clean build uploadArchives
