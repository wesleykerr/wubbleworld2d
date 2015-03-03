# Running Single Experiment #

So, you've checked out the Eclipse project and now you are curious how to run everything.  We will start by configuring the ` edu.arizona.simulator.ww2d.Main.java ` through the Run Configurations option in Eclipse.

Under the Arguments tab, we need to add several VM arguments in order to make everything work properly.  My personal VM arguments configuration looks as follows:

```
-Xmx2048m -Djava.library.path="natives/linux/"
```

The first argument increases the java heap size to 2GB.  We don't technically need that much space, but I really dislike program crashes because we've run out of heap space.

The second argument sets up the ` java.library.path ` to point to the natives directory.  Depending on your platform, you will specify a different directory.  For example if you are running on windows the correct directory will be ` natives/windows/ ` and for mac the correct directory is ` natives/macosx/ `.

You should now be up and running.  To change the configuration files, we currently just edit the ` Main.java ` file.  Look for the line that creates a new GameplayState and gives it several arguments.  One fo those arguments points to the location for the room initialization file and another points to the location for the agent initialization file.

# Recording Individual Runs #

This section will focus on the file ` edu/arizona/simulator/ww2d/Record.java `.  We want to be able to run multiple experiments have the system do a serious amount of logging for us.  To accomplish this, we need to make sure that we have installed [xuggler](http://www.xuggle.com/) which takes care of video recording for us.

In order to make Eclipse play nice with xuggler, we need to set up some additional parameters on the Run Configuration for Record.java.  Rather than write these down, I took two screen shots and have attached them.

![http://wubbleworld2d.googlecode.com/svn/trunk/docs/images/record-screen1.png](http://wubbleworld2d.googlecode.com/svn/trunk/docs/images/record-screen1.png)

This screen shot shows the Record configuration that we will be editing.  To get to this screen select Run Configuration from Run As.

![http://wubbleworld2d.googlecode.com/svn/trunk/docs/images/record-screen2.png](http://wubbleworld2d.googlecode.com/svn/trunk/docs/images/record-screen2.png)

Here we show the new environment variables that we need to set up so that the Record program can find the correct xuggler libraries and programs.  If you didn't follow the default installation for xuggler or are running on windows then you may have different paths.