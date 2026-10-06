# Privacy Policy

**Effective date:** October 6, 2026

This Privacy Policy explains how Lumen ("the app," "we," "us") handles information on your device. Lumen is an open source project. Its complete source code is available at https://github.com/AbrarShakhi/Lumen, so every statement in this policy can be independently verified.

## Summary

- Lumen does not have servers, user accounts, advertising, or analytics.
- Lumen does not sell, rent, or share your personal information.
- Searching your apps, contacts, files, calendar, notes, and settings happens entirely on your device.
- Information leaves your device only when you use a feature that requires the internet, as described below.

## Information Lumen Accesses on Your Device

Lumen reads the following information only to show search results to you. This information is processed on your device and is never transmitted by Lumen.

| Information | Permission | Purpose |
| --- | --- | --- |
| Installed apps and their shortcuts | None (package visibility) | Search and launch apps |
| Contacts | Contacts | Search contacts and start calls or messages |
| Calendar events | Calendar | Search upcoming events and join meeting links |
| Photos, videos, and audio | Photos and videos; Music and audio | Search media files by name |
| Documents in folders you choose | Access granted through the system folder picker | Search documents by name |
| Device settings screens | None | Open system settings quickly |

Each permission is optional. Lumen requests a permission only when you use the related search source, and you can revoke any permission at any time in your device settings. You can also turn off any search source in **Settings > Search sources**.

## Information Lumen Stores on Your Device

- **Notes** that you write.
- **Search index** of app names and document names, used to make searches fast.
- **Usage statistics** that record which results you open, used only to rank results you use often higher. These statistics never leave your device.
- **Preferences** such as theme, colors, and enabled search sources.
- **AI API key**, if you add one. The key is encrypted with a key held in the Android Keystore and is never stored in plain text.

Lumen excludes its database and preference files from Android cloud backup and device-to-device transfer. As a result, this information is not copied to Google servers by Android backup.

## Information That Leaves Your Device

Lumen connects to the internet only for the following features. Each request is sent directly from your device to the named service. Lumen does not operate any intermediary server.

### Currency Conversion

When you type a currency conversion, such as "100 usd in eur," Lumen requests current exchange rates from the Frankfurter API (https://www.frankfurter.app). The request contains only the base currency code. Your query, contacts, and other personal information are not sent. Frankfurter receives your IP address as part of the network connection. Its handling of that information is governed by its own policies.

### AI Answers

AI answers are disabled until you add your own Google Gemini API key. When you ask a question by typing `ai` followed by your question, Lumen sends the question text and your API key to the Google Gemini API (https://generativelanguage.googleapis.com). Google processes this request under its own terms and privacy policy, including the Gemini API Additional Terms of Service and the Google Privacy Policy (https://policies.google.com/privacy). Do not include sensitive personal information in questions you send to AI services.

### Web Search and Links

When you choose a web search result or open a link, Lumen hands the request to your web browser or the relevant app. The website or app you open then handles your information under its own privacy policy.

## Children's Privacy

Lumen does not knowingly collect any personal information from anyone, including children under the age of 13. Because Lumen does not collect personal information, it has no personal information to disclose.

## Data Retention and Deletion

All information that Lumen stores remains on your device until you delete it. You can delete individual notes in the app, remove your AI API key in **Settings > AI answers**, and remove document folders in **Settings > Document folders**. To delete all Lumen data, clear the app's storage in your device settings or uninstall the app.

## Security

Lumen relies on the security protections built into Android, stores your AI API key in encrypted form, and communicates with online services over encrypted HTTPS connections.

## Changes to This Policy

We may update this Privacy Policy from time to time. Changes will be published in the Lumen source repository and included in the next app release, with an updated effective date.

## Contact

If you have questions about this Privacy Policy, please open an issue at https://github.com/AbrarShakhi/Lumen/issues.
