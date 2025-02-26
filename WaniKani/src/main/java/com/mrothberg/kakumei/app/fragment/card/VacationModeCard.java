package com.mrothberg.kakumei.app.fragment.card;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mrothberg.kakumei.app.activity.Browser;
import com.mrothberg.kakumei.R;

/**
 * Created by Hikari on 8/21/14.
 */
public class VacationModeCard extends Fragment {

    View view;
    Context context;

    Button mDeactivate;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.card_vacation_mode, null);

        context = getActivity();

        mDeactivate = (Button) view.findViewById(R.id.vacation_mode_deactivate);

        mDeactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Browser.class);
                intent.putExtra(Browser.ARG_ACTION, Browser.ACTION_ACCOUNT_SETTINGS);
                context.startActivity(intent);
            }
        });

        return view;
    }
}
