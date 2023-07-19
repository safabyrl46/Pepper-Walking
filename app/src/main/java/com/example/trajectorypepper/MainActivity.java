package com.example.trajectorypepper;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import android.os.Bundle;
import android.util.Log;

    public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks  {
    private Animate animate;
    private GoTo goTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_hand_left_a001) // Set the animation resource.
                .build();

        animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build();
        Locale locale = new Locale(Language.TURKISH, Region.TURKEY);
        Phrase phrase = new Phrase("Cümleten selamun aleyküm!");
        Say say = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .withLocale(locale)
                .build();
        animate.run();
        say.run();


        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();
        Transform transform = TransformBuilder.create().fromXTranslation(0.45);

        Mapping mapping = qiContext.getMapping();

        // Create a FreeFrame with the Mapping service.
        FreeFrame targetFrame = mapping.makeFreeFrame();
        // Update the target location relatively to Pepper's current location.
        targetFrame.update(robotFrame, transform, 0L);

        goTo = GoToBuilder.with(qiContext) // Create the builder with the QiContext.
                .withFrame(targetFrame.frame()) // Set the target frame.
                .build(); // Build the GoTo action.

        goTo.addOnStartedListener(() -> Log.i(TAG, "GoTo action started."));
        Future<Void> goToFuture = goTo.async().run();

        goToFuture.thenConsume(future -> {
            if (future.isSuccess()) {
                Log.i(TAG, "GoTo action finished with success.");
            } else if (future.hasError()) {
                Log.e(TAG, "GoTo action finished with error.", future.getError());
            }
        });


    }

    @Override
    public void onRobotFocusLost() {

        // Remove on started listeners from the GoTo action.
        if (goTo != null) {
            goTo.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

}