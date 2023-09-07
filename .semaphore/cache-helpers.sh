function gradle-checksum {
  export GRADLE_CHECKSUM=`md5sum $(find . -name '*.gradle.kts' -or -name gradle.properties | sort) | md5sum | cut -d' ' -f1`
  echo "${GRADLE_CHECKSUM}"
}

function restore-gradle-cache {
  if [ -z "$GRADLE_CHECKSUM" ]; then
    gradle-checksum
  fi

  if [ "$CI" = true ]; then
    echo "Restoring gradle cache - checksum ${GRADLE_CHECKSUM}"
    cache restore gradle-build-cache-checksum-${GRADLE_CHECKSUM},gradle-build-cache-
  else
    echo "not running on CI, so skipping cache restoration"
  fi
}

function store-gradle-cache {
  if [ -z "$GRADLE_CHECKSUM" ]; then
    gradle-checksum
  fi

  ## Check if gradle Build cache is getting too large, and if so, clear it.
  local cache_threshold_mb=2048
  if [ $(du -sm ~/.gradle/caches/ | cut -f1) -gt $cache_threshold_mb ]; then
    if [ "$CI" = true ]; then
      rm -rf ~/.gradle/caches/*
      mkdir -p ~/.gradle/caches
      echo "clearing gradle build cache because it has grown to over $cache_threshold_mb MB"
    else
      echo "in ci, would have uploaded gradle build cache to semaphore cache (it is over  $cache_threshold_mb MB)"
    fi
  fi

  if [ "$CI" = true ]; then
    echo "uploading gradle build to cache with checksum $GRADLE_CHECKSUM"
    cache store gradle-build-cache-checksum-${GRADLE_CHECKSUM} ~/.gradle/caches
  fi
}
