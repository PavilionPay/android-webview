#  VIP Connect Plaid Integration Test App

This app is a scaffold to quickly and simply test various configurations for Plaid OAuth account linking in the VIP Connect SDK in a native app.

This app contains only two views natively, but does contain some additional setup and configuration code that may be useful when debugging.

## App Structure

The WebSDKTester app launches with a simple native View containing a button to launch the VIP Connect web SDK.
When the button is clicked, the user navigates to a screen containing a WebView. The VIP Connect SDK is agnostic
to how its containing WebView is presented, but for purposes of this app is is displayed in a simple Scaffold screen
with a top bar to return to the previous screen and restart the WebView.

The first screen after launching the web view is a dummy operator screen whose purpose is to create and launch a valid
VIP Connect session. Default values are provided but can be modified.

If you have obtained a session id from somewhere outside the app, you may simply enter it in the provided field and launch
VIP Connect with that session.

Otherwise, you will need to create a session in-app. The landing screen in the web view provides a button that will
create a session based on the current values for the `apiBaseUrl` and `transactionAmount` in the form.

In both cases, a VIP Connect instance will be launched at the given `sdkBaseUrl` with the given `redirectUrl`.

The VIP Connect SDK will load the session into the webview and display a funding page. From here, the Plaid integration
to the VIP Connect SDK can be tested.

## Plaid Integration Explanation

VIP Connect uses Plaid to connect bank accounts as funding sources for your VIP Connect ledger. When a user needs to connect a new
bank account, VIP Connect establishes a Link session with the Plaid web SDK, which will launch its own UI within the same web view.
Plaid will gain credentials to the bank account through either a username/password combo, an in-app OAuth web view, an in-browser
OAuth web view, or by authorizing within another app, all depending on how the specific bank is configured and what options are available on the user\'s device.

Once credentials have been granted, Plaid will continue to give VIP Connect access to the chosen bank account, and then return to the
VIP Connect UI where transactions may continue.

## Plaid Redirect URL

Upon completion of the bank\'s OAuth UI, and Plaid has gotten the credentials it needs, the user will need to be put back into the Plaid UI to
complete the connection of the bank account. The `redirectUrl` provided should lead to a page that is capable of restoring the VIP SDK session;
the default url will do this automatically for the user so they are brought back to the Plaid UI after OAuth is complete.
