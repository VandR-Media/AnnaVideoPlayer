# Huawei / APK security audit

## Confirmed finding in the 0.5.1 CI artifact

The GitHub Actions artifact `Annavue-0.5.1-debug` was a development build, not a release build.

Direct inspection of the APK showed:

- Android manifest: `android:debuggable=true`.
- APK signer certificate subject: `CN=Android Debug, O=Android, C=US`.
- Debug-only exported activity: `androidx.compose.ui.tooling.PreviewActivity`.
- The workflow did not produce a Release APK because the four `ANNAVUE_*` signing secrets were not configured.

The debug signing certificate SHA-256 fingerprint was:
`64bc7dafa5e6f266bb76ffc6ba47b98fe8598b77b7ef433569f4184790b5c99a`.

This fingerprint identifies the development certificate only; it is not a release identity.

## Changes in 0.5.2

- Debug APK remains available to CI tasks only and is no longer uploaded as a user-installable artifact.
- A user-installable artifact is produced only by the signed Release variant.
- Release verification checks APK signature, zip alignment, `debuggable=false`, and absence of Compose PreviewActivity.
- If signing secrets are missing, the workflow reports that no installable APK was produced.

## Huawei warning: what code can and cannot fix

A correct Release build removes the confirmed development-build problems. It cannot, by itself, guarantee that Huawei removes the warning shown for an APK installed through Files.

Huawei's AppGallery review guidance says that users may be notified of potential risks when an app is distributed through channels other than AppGallery. Huawei also provides a pre-release APK check that returns a security report; access requires a verified Huawei Developer / AppGallery Connect account.

Therefore the remaining authoritative step is to submit the signed Release APK to AppGallery Connect's pre-release check and, if the goal is to remove the sideload warning, complete AppGallery review/distribution. Do not disable Huawei's threat protection as a workaround.
