# BTT_BCalls
Application that scans a directory for wav files that fit a mask for uploading to a Broadcastify Calls node
<BR><BR>
Building:<BR>
ant jar<BR>
<BR>
Running:<BR>
java -jar BTT_BCalls.jar<BR>
<BR>
Description:<BR>
Monitors the specified directory for .wav files that match the mask for a Broadcastify Calls call and uploads the call.
.wav files are deleted after the upload. This can be used with any software that produces wav files with the correct
filename structure. Filename Mask: TG_ID_TIMESECS_SYSID_RID_DURATION_.wav<BR>
e.g. TG_317_1656616227_842_857937500_742467_6.5_.wav, where <BR>
<BR>
ID=TG ID, <BR>
TIMESECS=Time Since Unix Epoch Secs,<BR>
SYSID=System ID of the trunked system being monitored (decimal)<BR>
RID = Radio ID (decimal)<BR>
DURATION = Call duration seconds. (float)<BR>
<BR>
Requirements:<BR>
java runtime + ffmpeg executable<BR>
The ffmpeg executable can be downloaded from here: https://ffmpeg.org/download.html<BR>

ss1.png
<img src="https://github.com/bluetailtech/BTT_BCalls/blob/main/images/ss1.png">
ss2.png
<img src="https://github.com/bluetailtech/BTT_BCalls/blob/main/images/ss2.png">
