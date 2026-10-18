# Contributing

1. [Donations](#donations)
2. [Bug Reports and Feature Requests](#bug-reports-and-feature-requests)
3. [Spread the Word](#spread-the-word)
4. [Translations](#translations)
5. [Certificate of Origin](#certificate-of-origin)

## Donations

Does this app provide value? Suntimes is available gratis, but if it has proven its value, please pay as you feel.

* <a href="https://liberapay.com/forrestguice">Liberapay</a>

## Bug Reports and Feature Requests
Use the issue tracker to submit a bug report or feature request.

When reporting a bug **please be detailed as possible**. What did you expect the app to do, what did you actually observe? Include the app version number in your report. Other useful information includes the Android OS version (and sometimes your specific device model).

## Spread the Word
Share this app. Tell your friends. Let others know about F-Droid and the software that's available there.

## Translations

 1. Determine your locale code and values-folder-name (e.g. values-en-rGB is British English); see https://github.com/championswimmer/android-locales for a list of codes.

 2. Fork the master repository and create a new branch for your edits.

 3. Edit the `/res/<values-folder>/strings.xml` file using your favorite text editor. If it doesn't exist then create it and copy from `/res/values/strings.xml` as a starting template.

 4. Translate strings. If updating a translation, the lines that are marked `TODO` need translation or review.
   * Do not add values that don't already appear in the default `/res/values/strings.xml`.
   * Do not translate values that are marked `translatable="false"`. These values should only exist in the default `/res/values/strings.xml`.
   * Do not translate values between `<xliff:g>` tags. These are either parameters or constant values that must remain the same.
   * For `string-array` values be mindful not to change the order of the entries. These `string-arrays` are usually mapped (one-to-one) with non-translatable values in the default `/res/values/strings.xml`.
<br /><br />
 5. Add your name to the credits.
  * For new translations, add an item to `locale_credits`, and corresponding items to `locale_values`, `locale_display`, and `locale_display_native` (in the default `/res/values/strings.xml`).
  * For existing translations, append your name to the corresponding line in `locale_credits` (using a `|` as a delimiter).
  <br /><br />
 6. Submit a pull request.

## Certificate of Origin
Contributors certify that the work they submit to this project is their own, does not contain outputs from generative AI, and that they have full rights to submit that work under an open source license.
