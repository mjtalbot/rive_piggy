# rive_piggy

This is a sample Rive app for Android. 

The simple to use [RiveAnimationView](https://github.com/rive-app/rive-android/blob/master/kotlin/src/main/java/app/rive/runtime/kotlin/RiveAnimationView.kt) allows you to play animations or state machines in a couple of lines of code. This is great to get started with and check out the [help center](https://help.rive.app/runtimes/quick-start). 

The animation view is great for simple use cases, but in this example we want to shows how to use the [core](https://github.com/rive-app/rive-android/tree/master/kotlin/src/main/java/app/rive/runtime/kotlin/core) parts of the animation library to make a custom animation loop with far more control!

![piggy](https://user-images.githubusercontent.com/1216025/135996961-661949f8-974e-4f22-a9bc-adbb4a6e45cb.gif) 

# [Piggy.riv](https://github.com/mjtalbot/rive_piggy/tree/main/app/src/main/res/raw/piggy.riv) file set up. 

The piggy .riv file is split into 3 artboards

- Background
- Piggy
  - has a state machine "PiggyMachine" with a "Pressed" trigger input. The trigger input switches the pig from running the "idle" animation to running the "coin" animation.
- Coin
  - has a state machine "CoinMachine" which runs an animation of a coin flying up and back down again. 

This animation is setup so that you can draw the background first and layer the Piggy animation ontop of it. 
When you want to animate a coin, we add a new coin artboard onto our canvas and animate its coinmachine animation. This artboard will be removed once the animation is played. We also run the "Pressed" input for the PiggyMachine, to make the pig interact with the press events a little bit. 

# Same thing, but in JS

Luigi's original [example](https://codesandbox.io/s/piggy-htzfc?file=/src/index.js) of this for the web. There are great comments in the code here to understand how this works for javascript. 
