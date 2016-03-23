#!/bin/bash

ideaVersion="15.0.4"

if [ ! -d ./idea ]; then
    # Get our IDEA dependency
    wget http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz

    # Unzip IDEA
    tar zxf ideaIU-${ideaVersion}.tar.gz
    rm -rf ideaIU-${ideaVersion}.tar.gz

    # Move the versioned IDEA folder to a known location
    ideaPath=$(find . -name 'idea-IU*' | head -n 1)
    mv ${ideaPath} ./idea
fi

if [ ! -d ./plugins ]; then
    # Download required plugins
    mkdir plugins

    wget http://plugins.jetbrains.com/files/6610/22827/php-143.1184.87.zip

    unzip -qo php-143.1184.87.zip -d ./plugins
    rm -rf php-143.1184.87.zip
fi

# Run the tests
if [ "$1" = "-d" ]; then
    ant -d -f build-test.xml -DIDEA_HOME=./idea
else
    ant -f build-test.xml -DIDEA_HOME=./idea
fi

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    ant -f build-test.xml -q clean

    if [ "$1" = "-r" ]; then
        rm -rf idea
        rm -rf plugins
    fi
fi

# Return the build status
exit ${stat}
