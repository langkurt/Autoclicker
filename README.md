# Autoclicker
Record any taps/clicks on the screen. Then, play those actions and AutoClicker will run those taps/clicks automatically in the same order, same time, and same position.

Get started by cloning the repo and building the app on your android device.
Then, start the AutoClicker service under "Accessibility" in the system settings.
Finally, tap "record" and "start" to begin saving a workflow

TODO:
- Ability to change the playlist name after creation
- Confirm on delete
- Need to track these todo's better, but this is good for now
- Notification on playlist complete
- Playlist Description with CRUD
- Sometimes the taps never get played and I have to restart the service from android settings. Would be nice if the app had option to self restart
- Add Label locations, then build a playlist in some sort of editor. 
    For example, custom define [324, 638] as "myTap1", then define playlists as 
    [{location: myTap1, delay: 0.25}, {location: myTap2, delay: 1.0}, {location: myTap3, delay: 0.5},]

UI needs a lot of work as well
- Change "Start" button title to "recording" or "{name of playlist}"
- Change background of floating view to indicate current action performed
- Main activity list view and record button need to be styled
- Whatever else I can think of
