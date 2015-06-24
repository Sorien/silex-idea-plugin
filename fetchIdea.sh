#!/bin/bash

ideaVersion="14.1.4"

if [ ! -d ./idea ]; then
    # Get our IDEA dependency
    if [ -f ~/Tools/ideaIU-${ideaVersion}.tar.gz ];
    then
        cp ~/Tools/ideaIU-${ideaVersion}.tar.gz .
    else
        wget http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz
        # wget http://download.labs.intellij.net/idea/ideaIU-${ideaVersion}.tar.gz
    fi

    # Unzip IDEA
    tar zxf ideaIU-${ideaVersion}.tar.gz
    rm -rf ideaIU-${ideaVersion}.tar.gz

    # Move the versioned IDEA folder to a known location
    ideaPath=$(find . -name 'idea-IU*' | head -n 1)
    mv ${ideaPath} ./idea

    # Download required plugins
    mkdir plugins

    wget http://www.sorien.sk/plugins/php.tgz

    tar zxf php.tgz
    rm -rf php.tgz
    mv php ./plugins/php
fi