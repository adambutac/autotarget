# autotarget

This is an app that I wrote a few summers ago to pass the time.

Using an open source library <a href="http://openimaj.org/">Openimaj</a> I was able to capture data from my camera,
and use powerful image manipulation algorithms all in the comfort of java.

An image is taken every few seconds to use as a "mask" against the live camera feed
to highlight the differences between them, and thus movement (with surprising accuracy).

If the distance to the moving object is known and the camera remains still, we can approximate
where the object is, its velocity, and its acceleration in a 2d space (the distance to the 
object must remain constant).

Knowing this, we can approximate the objects coordinates relative to the camera. 
With some trig, we can then translate the coordinates into angles.
Using these angles, we can then create a signal that will direct a servo motor to set its angle
so that it points its arm towards the object.

For simplicity, I decided to use the audio bus as my signal output.

