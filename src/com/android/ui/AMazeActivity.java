package com.android.ui;

import com.android.ui.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

/** The Landing screen on the Android app, prompts user for
 * skill level and operation mode
 * @author adam
 *
 */
public class AMazeActivity extends Activity {
	private static final String TAG = "AMazeActivity";
	Button btn;
	TextView selection1, selection2;
	int skill, mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/** skill level spinner **/
		selection1 = (TextView) findViewById(R.id.selection1);
		Spinner spin1 = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.skill, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin1.setAdapter(adapter);

		spin1.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        switch(position){
		        	case 0: skill = 0; break;
		        	case 1: skill = 1; break;
		        	case 2: skill = 2; break;
		        	case 3: skill = 3; break;
		        	case 4: skill = 4; break;
		        	case 5: skill = 5; break;
		        	case 6: skill = 6; break;
		        	case 7: skill = 7; break;
		        	case 8: skill = 8; break;
		        	case 9: skill = 9; break;
		        	default: skill = 0; break;
		        }
		        Log.v(TAG, "skill = " + skill);
		        
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        skill = 0;
		    }

		});
		
		/** operation mode spinner **/
		selection2 = (TextView) findViewById(R.id.selection2);
		Spinner spin2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.modes, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin2.setAdapter(adapter2);
		
		spin2.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        switch(position){
		        	case 0: mode = 0; break;
		        	case 1: mode = 1; break;
		        	case 2: mode = 2; break;
		        	case 3: mode = 3; break;
		        	case 4: mode = 4; break;
		        	default: mode = 0; break;
		        }
		        Log.v(TAG, "mode = " + mode);
		        
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    	mode = 0;
		    }

		});

		/** Launch button sends us to the MazeBuilder Activity **/
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(AMazeActivity.this,
						MazeBuilderActivity.class);
				Bundle b = new Bundle();
				b.putInt("skill", skill);
				b.putInt("mode", mode);
				i.putExtras(b);
				startActivity(i);
			}
		});

	}

}
