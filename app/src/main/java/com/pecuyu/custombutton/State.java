package com.pecuyu.custombutton;

/**
 * Created by pecuyu on 2017/9/21.
 */

public interface State {
    void animateView();

    void publishListener(CustomButton.OnStateChangeListener listener);
}
