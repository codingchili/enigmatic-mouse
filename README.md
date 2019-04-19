# Enigmatic Mouse [![Build Status](https://travis-ci.org/codingchili/enigmatic-mouse.svg?branch=master)](https://travis-ci.org/codingchili/enigmatic-mouse)
The enigmatic mouse will keep your passwords safe - password manager on Android written in Kotlin.

 View the [YouTube Demo](https://www.youtube.com/watch?v=CcIvlFmBL5w).

![mouse enigma preview](https://raw.githubusercontent.com/codingchili/enigmatic-mouse/master/preview.jpg "Current snapshot version")

Password manager in 1500 lines of KOTLIN!

The Enigmatic Mouse is a small password manager, the purpose is to be as small as possible
while still providing a bearable user experience. By being small The Mouse is auditable by
our user base. No need to trust a third party with the keys to the kingdom, you can fork
the repository and add new features or even disable existing ones! For maximum security
we recommend that you build and side-load the application yourself. This ensures that
a rogue version published to the Play store won't steal all your passwords.

Requires SDK26 (can probably be built with lower API levels too.)

# Features
- application is protected by fingerprint authentication.
- securely store passwords encrypted within Realm.
- shows icons for the sites you add from the internet.
- allows you to copy to clipboard or view passwords within the app.
- set a credential as favorite and sticky it to the top of the list.
- downloads the haveibeenpwned domain list and compares with your accounts.

# Security
The encryption scheme

The master password is combined with a key derivation function (Scrypt) to generate an AES key of 256 bits.
Another key is then created within the Trusty TEE (HSM) and used to encrypt the AES key.
The key stored in TEE is protected by your fingerprint and never leaves the HSM.
We store the encrypted key, the salt used with the master password and the 
initialization vector used as shared preferences. This information is not a 
cryptographic secret. When the user authenticates with their fingerprint, we use the
AES key stored in the HSM to decrypt the key derived from the master password. When the
master key is recovered, we initialize the Realm encrypted database with it.

### Features
- Fingerprint authentication
- Scrypt, N=65536, r=8, p=1
- Realm - encrypted with AES256 key.
- AES256-CBC-PKCS7
- Trusty TEE

### Permissions

The following permissions are required.
```xml
<uses-permission android:name="android.permission.USE_BIOMETRIC"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

The biometric permissions is used to authenticate with the Trusty TEE (HSM)
using a fingerprint. In newer versions of Android there might be more ways
to authenticate with biometrics.

The Internet permission is used to download icons from websites. For example
if you add a credential for youtube.com -> we will fetch the index page from 
youtube and parse any `<link rel="icon" href="..."` elements and select the 
biggest available icon. If you think Internet permissions is scary in your password
manager you can remove it. The feature to check if a site has been hacked also
depends on this permission "security lists".

# Building
Open the project in Android Studio, Build -> Make Project.

Without Android studio,
```console
./gradlew build
```

Find the unsigned .apk in ```app\build\outputs\apk\release```.

# Installing

Installing the application yourself is the recommended way, as it removes the middleman.

##### Side-loading (Android studio)
- Open the project with android studio -> run -> select your device

This will build the APK and install it onto your device.

##### Side-loading (APK file)
Follow the instructions for building an unsigned APK and then copy the .apk to your device. Alternatively download
a signed APK from the releases.

1. Enable installation from untrusted sources
2. open the file to install the APK
3. Disable installation from untrusted sources

##### Google Play store
Now published on the play store!

[![Enigmatic Mouse @ Play Store](https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.codingchili.mouse.enigma)

# Contributing
Contributions are welcome! We encourage you to look through the available issues,
create new or comment on existing. All ideas are welcome and well needed.

Code reviews and security audits are also very welcome.

[![donate](https://img.shields.io/badge/donate-%CE%9ETH%20/%20%C9%83TC-ff00cc.svg?style=flat&logo=ethereum)](https://commerce.coinbase.com/checkout/673e693e-be6d-4583-9791-611da87861e3)

# Resources
During development the following talk has been very helpful in implementing the security scheme.

Ben Oberkfell - Advanced Android Fingerprint Security | Øredev 2017
[https://vimeo.com/243345710](https://vimeo.com/243345710)

[benoberkfell/CryptoDiary](https://github.com/benoberkfell/CryptoDiary)
