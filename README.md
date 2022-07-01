# BTT_BCalls
Application that scans a directory for wav files that fit a mask for uploading to a Broadcastify Calls node

Building:
ant jar

Running:
java -jar BTT_BCalls.jar

Description:
Monitors the specified directory for .wav files that match the mask for a Broadcastify Calls call and uploads the call.
.wav files are deleted after the upload. This can be used with any software that produces wav files with the correct
filename structure. Filename Mask: TG_ID_TIMESECS_SYSID_RID_DURATION_.wav 
e.g. TG_317_1656616227_842_857937500_742467_6.5_.wav, where 
ID=TG ID, 
TIMESECS=Time Since Unix Epoch Secs,
SYSID=System ID of the trunked system being monitored (decimal)
RID = Radio ID (decimal)
DURATION = Call duration seconds. (float)

Requirements:
java runtime + ffmpeg executable
The ffmpeg executable can be downloaded from here: https://ffmpeg.org/download.html
