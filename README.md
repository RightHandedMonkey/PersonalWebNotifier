# PersonalWebNotifier
NOTE: This product is in Alpha development and is partially complete

# New

This app has been completely rewritten using Google Room and can be used as an example of how easy it is to work with Room
Room: https://developer.android.com/topic/libraries/architecture/room.html  

This is an open source Android app for programmers to setup notifications for websites that watches for changes and alerts you on your device

It illustrates using:
* Room for retrofit-like database access
* Android JobScheduler to handle the background updates
* Boot Complete Receiver to continue checks after a reboot
* RxJava2 Completables to replace ASyncTask

Immediate goals:
* Indeterminate loading indicator on webview page
* Tooltip on webview page first load to indicate to click when page loads
* Getting started dialog to show how the app works/what it is for
* Start with sample with tooltip and have button to click to select CSS instead of requesting user to enter text

Future goals:
* Rewrite using MVP
* Implement Dagger2

-SamB
