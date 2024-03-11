# Step2UCS
_A project by @rilegoat to make .ucs filewriting easier_

## Background
Andamiro's "Pump It Up" series of arcade dance games offers a User Custom Step (UCS) feature in which players can create their own charts for songs featured in-game by using a software titled StepEdit Lite. Unfortunately, StepEdit Lite is extremely antiquated, and as someone that is spoiled by the functionality and ease-of-use of charting software such as ArrowVortex, I grew tired of its clunkiness. As a result, I decided to write a converter to allow users to convert .sm files to the .ucs format, with the goal of alleviating the need to use StepEdit Lite at all.

## How to Use
**Note: This software requires Java Runtime Environment 1.8.0 or above to work.**


**IMPORTANT**<br>
⚠️ As of v0.1, pressing "Generate" will completely format the .ucs file before converting the .sm data in. I strongly urge you to convert into a **new, unmodified sample .ucs file** so you do not lose any important data, because different formats of .sm may not be compatible with this converter yet, which may leave the .ucs file totally blank in the result of a faulty conversion.


First, download the compiled .jar app here:<br>
[link to download will be provided once the software is more stable! In the meantime, you can try downloading and compiling the source code yourself if you insist on trying the unstable version.]


After opening the app, you'll see several buttons down the right side of the window along with a "Generate" button at the bottom. 

1. First, open your .ucs sample file by clicking the first "open" button along the right. This .ucs should be the sample file you receive from the Pump It Up UCS website. *Do not make any modifications to it after downloading and extracting!* This file has the correct offset for playback on cab, as well as the correct filename for uploading to the website. To be sure your file loaded correctly, after opening the .ucs sample file, the two lines at the bottom of the window should show the file name as well as the correct initial offset in milliseconds.

2. Next, open the .sm file you want to convert to .ucs by clicking the next "open" button along the right. This .sm file should:
    * only have one difficulty
    * be either Pump Single, Pump Double, or Pump Coop
    * not have sm5 gimmicks (scrolls, speeds, warps, fakes)---though these are removed when you save to .sm anyways    
    * be made using the audio provided in the .ucs sample download, or be made using audio that does not have a long initial silence---I only recommend using the .ucs sample audio because the initial silence matches what is played on cab

3. Finally, click the bottom "Generate" button to convert your .sm file, which will then **overwrite** the sample .ucs file you chose in step 1.


:warning: Please note that the app will most likely break in its current state if you try to convert multiple files sequentially without closing and reopening the app.


After your .ucs file has been converted, navigate to the PIU UCS website and upload the newly overwritten .ucs file---you should not have to make any modifications to it. For the first few builds of this software, I would recommend doublechecking the file in StepEdit Lite before uploading, but eventually it should be stable enough that you can get by without ever using StepEdit Lite. 

