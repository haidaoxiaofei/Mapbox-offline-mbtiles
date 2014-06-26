### API Documentation

The API documentation is generated via script from Javadocs. To update 
the existing documentation run:

1. Update `_config.yml` [version and snapshot lines](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/_config.yml#L7-L8)
2. Update `` [version and snapshot lines](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/_config.mb-pages.yml#L7-L8)
3. Run the following on Comamnd Line

```bash
    cd docs/_build
    ./build VERSION
```

`VERSION` should be replaced with the latest Mapbox Android SDK version (ie 0.2.3).
UP
