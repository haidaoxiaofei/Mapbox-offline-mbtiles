Documentation
---

The API documentation is generated via script from Javadocs. To update 
the existing documentation you'll need to do the following:

- On OSX? Install [gnu-sed](https://sagebionetworks.jira.com/wiki/display/PLFM/Fixing+sed+on+OSx).
- Update [the version key](https://github.com/mapbox/mapbox-android-sdk/blob/9a04e30cef45d602f3e67a237f7d877c210f5e11/_config.yml#L8) in `_config.yml`.
- Update [the version key](https://github.com/mapbox/mapbox-android-sdk/blob/9a04e30cef45d602f3e67a237f7d877c210f5e11/_config.mb-pages.yml#L8) in `_config.mb-pages.yml`.
- Scrape and build the docs with

```sh
    cd docs/_build
    ./build 0.your.version
```
