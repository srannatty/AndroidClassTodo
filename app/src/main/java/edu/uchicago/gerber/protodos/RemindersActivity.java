package edu.uchicago.gerber.protodos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class RemindersActivity extends ActionBarActivity {


    private ListView mListView;
    private RemindersDbAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        //Register Context Menu
        registerForContextMenu(mListView);

        mDbAdapter = new RemindersDbAdapter(this);
        mDbAdapter.open();
        if (savedInstanceState == null) {
            //Clean all data
            mDbAdapter.deleteAllReminders();
            //Add some data
            insertSomeReminders();
        }

        Cursor cursor = mDbAdapter.fetchAllReminders();

        //from columns defined in the db
        String[] from = new String[]{
                RemindersDbAdapter.COL_CONTENT
        };

        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };

        mCursorAdapter = new RemindersSimpleCursorAdapter(
                //context
                RemindersActivity.this,
                //the layout of the row
                R.layout.reminders_row,
                //cursor
                cursor,
                //from columns defined in the db
                from,
                //to the ids of views in the layout
                to,
                //flag - not used
                0);


        //the cursorAdapter (controller) is now updating the listView (view) with data from the db (model)
        mListView.setAdapter(mCursorAdapter);
    }

    private void insertSomeReminders() {
        mDbAdapter.createReminder("Buy Learn Android Studio", true);
        mDbAdapter.createReminder("Send Dad birthday gift", false);
        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
        mDbAdapter.createReminder("String squash racket", false);
        mDbAdapter.createReminder("Shovel and salt walkways", false);
        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
        mDbAdapter.createReminder("Buy new office chair", false);
        mDbAdapter.createReminder("Call Auto-body shop for quote", false);
        mDbAdapter.createReminder("Renew membership to club", false);
        mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
        mDbAdapter.createReminder("Sell old Android phone - auction", false);
        mDbAdapter.createReminder("Buy new paddles for kayaks", false);
        mDbAdapter.createReminder("Call accountant about tax returns", false);
        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
        mDbAdapter.createReminder("Call the Dalai Lama back", true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
        return true;
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            getMenuInflater().inflate(R.menu.menu_context, menu);
    }



    public void updateListView() {
        Cursor cursor = mDbAdapter.fetchAllReminders();
        mCursorAdapter.changeCursor(cursor);
        mCursorAdapter.notifyDataSetChanged();
        mListView.invalidateViews();
        mListView.refreshDrawableState();
    }


    public void makeDialog(String title, final Boolean editing, final Reminder reminder) {
        Log.d(getLocalClassName(), "make Dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflatedDialogView = View.inflate(this, R.layout.dialog_todo, null);

        final EditText input = (EditText) inflatedDialogView.findViewById(R.id.edit_text_todo);
        final ArrayList checkImportant = new ArrayList();

        //make a dummy Reminder first
        Log.d(getLocalClassName(), "Making Checkbox");
        CheckBox checkBox = (CheckBox) inflatedDialogView.findViewById(R.id.checkbox);

        //initialize using reminder
        Log.d(getLocalClassName(), "init stuff");
        if (editing) {
            input.setText(reminder.getContent(), TextView.BufferType.EDITABLE);
            if (reminder.getImportant() == 1) {
                //set checkbox to be checked in default.
                checkBox.setChecked(true);
            }
        }

        Log.d(getLocalClassName(), "make Checkbox listener");
        //checkbox options
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(getLocalClassName(), "isChecked: " + isChecked);
                reminder.setImportant(isChecked? 1:0);
            }
        });
        checkBox.setText(" Important");

        Log.d(getLocalClassName(), "building dialog");
        builder.setView(inflatedDialogView)
                // Add action buttons
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        reminder.setContent(input.getText().toString());
                        if (editing) {
                            mDbAdapter.updateReminder(reminder);
                        } else {
                            mDbAdapter.createReminder(reminder);
                        }
                        updateListView();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });

        Log.d(getLocalClassName(), "Create dialog");
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.d(getLocalClassName(), "end makeDialog");
    }



    public void editTodo(int id) {
        final Reminder todo = mDbAdapter.fetchReminderById(id);

        //Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final CharSequence[] item = {" Important "};
        final ArrayList selectedItem = new ArrayList();

        //initialize using the information in the old reminder
        //text init
        input.setText(todo.getContent(), TextView.BufferType.EDITABLE);
        //important init
        //todo initialize this
        int important = todo.getImportant();
        if (important == 1) {
            selectedItem.add(0);
        }

        builder.setTitle(R.string.edit_reminder)
                .setView(input)
                .setMultiChoiceItems(item, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            // write your code when user checked the checkbox
                            //indexSelected is always 0 (only one item)
                            selectedItem.add(indexSelected);
                        } else if (selectedItem.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            // write your code when user Unchecked the checkbox
                            selectedItem.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })

                // Set the action buttons
                .setPositiveButton("Commit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  when user clicked on OK
                        todo.setContent(input.getText().toString());
                        todo.setImportant(selectedItem.contains(0) ? 1 : 0);
                        mDbAdapter.updateReminder(todo);
                        updateListView();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
        // 4. create alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.cnt_menu_edit:
                //editTodo((int)info.id);
                Reminder oldReminder = mDbAdapter.fetchReminderById((int)info.id);
                makeDialog("Edit Reminder", true, oldReminder);
                return true;
            case R.id.cnt_menu_delete:
                mDbAdapter.deleteReminderById((int)info.id);
                updateListView();
                return true;
            default:
                Toast.makeText(this, "default is called", Toast.LENGTH_SHORT).show();
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ic_new:
                Log.d(getLocalClassName(), "create new Reminder from action icon");
                Reminder reminder = new Reminder(0,"",0);
                makeDialog("New Reminder", false, reminder);
                return true;
            case R.id.action_new:
                Log.d(getLocalClassName(), "create new Reminder");
                Reminder reminder2 = new Reminder(0,"",0);
                makeDialog("New Reminder", false, reminder2);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }


}
