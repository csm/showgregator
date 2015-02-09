# Time Zone handling

Basic idea: all users have a IANA time zone ID, of varying quality. We first try location based on IP address; then location supplied by the browser; then user-supplied time zone.

## Transient users

* Inherit initial location from user that invited this transient user.
* Update based on IP address, if good quality.
* Update based on browser-supplied geolocation, if given.
* Allow override to any static time zone.

## Regular users

* Inherit any status coming from transient user, if any.
* Use