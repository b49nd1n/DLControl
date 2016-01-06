package ru.mgvk.dlcontrol;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * Класс создания прибора
 * 
 * @author Michael_Admin
 *
 */

public class FixtureCreating extends Fixture{

	int functcount=0;
	
	
	
	LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
	
	
	
	
	Context context;

	MainActivity MAct;
	
	
	public FixtureCreating(Context parentcontext) {
		context = parentcontext;
		MAct = (MainActivity) parentcontext;
	
	}
	
	public class onAddFuncClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			LinearLayout FunctLayout = new LinearLayout(context);
			MAct.FCLayout.addView(FunctLayout, params);
			Button btn = new Button(context);
			btn.setText("NAN");
			btn.setLayoutParams(params);
			MAct.FC2Layout.addView(btn);
		}
    }
	
	public class onDelFunctClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class onSaveFixtClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	
}