# Parkitect Blueprint Investigator

This is an academic investigation of how the blueprints in [Parkitect](https://store.steampowered.com/app/453090/Parkitect/) are stored. 
It started because I misspelled "castle" as "castke".

- [Step 1 - File Search](#step-1---file-search) 
- [Step 2 - Analyzing the PNG Chunks](#step-2---analyzing-the-png-chunks) 
- [Step 3 - Investigating RGB Bits](#step-3---investigating-rgb-bits) 
- [Step 4 - Finding the File Name](#step-4---finding-the-file-name) 
- [Step 5 - Using Brute Force](#step-5---using-brute-force)  


## Step 1 - File Search

First we need to find out which files contain the relevant data, so playing with the 
game brings us to this conclusion:

- blueprints are stored (on Windows) in the directory _C:\Users\<Username>\Documents\Parkitect\Saves\Blueprints_ as PNG
- if you copy one of the blueprints there, it will show up as the original name in the game and work (so the data seems to be stored in the PNG)
- however if you delete a blueprint there, it will be recreated when you next start the game (which could be due to to 
  the steam cloud synchronizing their backup of the savegames back onto your PC)
- if you delete a blueprint in game, only the PNG file will be removed in the _Saves_ directory and one byte changes in the _workshop\_files.dat_ - which seems to little to contain how the blueprint data is stored (my guess would be either a "size" field or a time stamp)

So at this point the most probable location for the blueprint data is the PNG file.


## Step 2 - Analyzing the PNG Chunks
    
<i>See [Step2ImageChunksCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step2ImageChunksCheck.java)</i>

After realizing the data would be in the PNG, I did what every programmer likes to do 
- read the [PNG spec](http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html). What 
a treat!

PNG allowes for custom chunk data, and if this chunk data is unknown to an decoder, 
but it can safely be ignored, then an "ancillary bit" is set to true. Perfect, that's 
the chunks to put game data into.

So I created a little program to read the chunks according to spec. Running [Step2ImageChunksCheck](./src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step2ImageChunksCheck.java) gets 
the following result:

```
File is really PNG.


Chunk 1
=======
Length: 13
Type: IHDR
	Ancillary bit: false


Chunk 2
=======
Length: 32768
Type: IDAT
	Ancillary bit: false


Chunk 3
=======
Length: 32768
Type: IDAT
	Ancillary bit: false


Chunk 4
=======
Length: 32768
Type: IDAT
	Ancillary bit: false


Chunk 5
=======
Length: 15089
Type: IDAT
	Ancillary bit: false


Chunk 6
=======
Length: 0
Type: IEND
	Ancillary bit: false
```

In short:

- the blueprints created in game have one IHDR, four IDAT and one IEND chunk
- the IDHR chunk is 13 bytes long and the IEND chunk 0 bytes, which is what is expected 
after reading the spec
- three of the four IDAT chunks have a length of 32768, which seems to be a limit and completely according to spec, the last chunk has a variable length
- there is no unknown chunk and no chunk with ancillary bit, which I expected to contain 
the game data

I checked the bytes of the files for the blueprint name, but couldn't find it, so in 
summary, nothing in the PNG data seems "off" in a way that screams "the game data is stored here". 

Great, now what?


# Step 3 - Investigating RGB Bits

<i>See [Step3RgbDataCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step3RgbDataCheck.java)</i>

A [question on Reddit](https://www.reddit.com/r/ThemeParkitect/comments/qpa35q/how_are_blueprints_stored/) 
revealed that the developers of Parkitect wrote about the subject: [Update 58](https://themeparkitect.tumblr.com/post/126855975857/update-58)

They explained the reason for not using custom chunks is that these are often removed 
by overzealous decoder, e.g. the ones used by uploaders on Tumbler.

So instead of using custom chunks, they used the last bits of the RGB fields. Very, 
very clever.

Since the Tumblr post is six years old, the first check is to see if my blueprints still 
contain the artifacts that show that the bit values of RGB are used:

![artifacts](no-blueprint/artifacts.png)
	
They do.

So I created a programm to get the last bits of the RGB values and print them to console. Running 
will print:

<kbd>![water-tower-data](no-blueprint/water-tower-data.png)</kbd>

...which looks like it could be game data. Or it's random. At least it looks like the 
last bytes are a repeating sequence, but that might be because it's the same color in 
the picture.

Just to be sure I ran the same programm over this GitHub profile picture, and the result 
is:

<kbd>![water-tower-data](no-blueprint/slothsoft-profile-data.png)</kbd>

...which looks a lot less like game data. 

However, if I search the blue print file's data for the blueprint name, which I know 
is there, then nothing is found. So either my algorithm is wrong, the last two 
bits (instead of last bit) is used, the order of the bits is incorrect or... the name is not 
stored in an ASCII representation.


# Step 4 - Finding the File Name

<i>See [Step4OneBitSearchCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step4OneBitSearchCheck.java), [Step4TwoBitSearchCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step4TwoBitSearchCheck.java)</i>


At this point it's clear the only things to do to figure this puzzle out is to

1. ask the persons that created the format
1. try to brute force my way in

Brute forcing could work via the assumption that the blueprint name is stored somewhere in
the image (which can easily be proven by copying or renaming the file - new copy has the same name as the
original, even though its file name differs).

So I created two classes that search for bytes with the same amount of ones as in the characters
of the blueprint name. 

No matter if I use the last bit ([Step4OneBitSearchCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step4OneBitSearchCheck.java)) 
or the last two bits ([Step4TwoBitSearchCheck](/src/main/java/de/slothsoft/parkitect/blueprint/investigator/Step4TwoBitSearchCheck.java)),
the blueprint name cannot be found.

Even if I fiddle with the order of the bits (not RGBA as in the Tumblr post, but ARGB or even
more outlandish combinations), I can't find the name. 

Maybe the encoding of the name is the problem...?



# Step 5 - Using Brute Force

So which encoding does the blueprints use? Hard to say. They could use some custom format, 
which is unlikely if a lot of characters are supported. So back in the game I tried to figure out
if symbols and umlauts are allowed. The result:

![water-tower-data](no-blueprint/content-manager-umlauts.png)

So it's not ASCII. But it is bound by the actual file name, because by default file name and blueprint 
name are the same. So which format do OS use?

Windows seems to use UTF-16, Mac and Linux UTF-8, so checking for UTF-8 should work better.

    
    