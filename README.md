# Snapsassin

Android app to moderate and play Assassin games using face recognition.

Like the live-action game, Assassin, but moderated by a mobile app. No one has to sit out to moderate the game--Everyone can play! Rather than shooting each other with squirt guns or throwing dirty socks around, this game is played with your phone camera. Snap a photo of your target and their identity will be verified using face recognition. If that's the right person, you've successfully assassinated your target!

TODO:

Firebase push notifications.
Should consider switching face recognition API from SkyBiometry to Microsoft Face.
Also should consider using Firebase storage as opposed to AWS storage.
Revamp the login system so its not hacky and uses more than just Facebook.
Implement veto system to veto cheats
Display history of photos so that everyone can verify that the snap was legitimate
Currently has a few bugs, mostly because of the tedious process of photo upload:

Take a photo
Upload photo to AWS
Get link to photo on AWS
Send link to SkyBio face recognition API with name of target
If successful recognition, update target status on Firebase and assign new target
(incomplete) Send a notification to everyone