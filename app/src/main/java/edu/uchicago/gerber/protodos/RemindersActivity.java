package edu.uchicago.gerber.protodos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
                //TODO call to edit an item
                editTodo((int)info.id);
                Toast.makeText(this, "(hopefully) id of this item is"+ info.id, Toast.LENGTH_SHORT).show();
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

    public void NewTodoDialog() {

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. User inputs what to do
        // text for what to do
        final EditText input = new EditText(this);
        final CharSequence[] item = {" Important "};
        // arraylist to keep the selected items
        final ArrayList selectedItem = new ArrayList();


        // 3. Set dialog characteristics.
        //todo important comes after the text part, which should be opposite.
        builder.setTitle(R.string.new_reminder)
                .setView(input);

        builder.setMultiChoiceItems(item, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    // write your code when user checked the checkbox
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
                        mDbAdapter.createReminder(input.getText().toString(), selectedItem.contains(0));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                Log.d(getLocalClassName(), "create new Reminder");
                NewTodoDialog();
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }


}
