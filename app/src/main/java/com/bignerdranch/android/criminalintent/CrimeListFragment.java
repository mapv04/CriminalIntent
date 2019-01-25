package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallback;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private OnDeleteCrimeListener mDeleteCallBack;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks{

        void onCrimeSelected(Crime crime);
    }

    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback= (Callbacks) context;
        mDeleteCallBack = (OnDeleteCrimeListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { //this tell your fragment manager that yor fragment should receive a call to onCreateOptionMenu(...)
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_crime_list,container,false); //layout con el recycler view
        mCrimeRecyclerView=(RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        setCrimeRecyclerViewItemTouchListener();

        updateUI();

        return view;
    }

    public void setCrimeRecyclerViewItemTouchListener(){
        ItemTouchHelper .SimpleCallback itemTouch = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Crime crime = mAdapter.mCrimes.get(position);
                mDeleteCallBack.onCrimeIdSelected(crime.getmId());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                final ColorDrawable background = new ColorDrawable(Color.RED);
                int dx = (int) dX;
                background.setBounds(0, itemView.getTop(),   itemView.getLeft() + dx, itemView.getBottom());
                background.draw(c);
                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.delete_crime);
                // compute top and left margin to the view bounds

                icon.setBounds(viewHolder.itemView.getLeft()-50 , viewHolder.itemView.getTop(), viewHolder.itemView.getRight()-800, viewHolder.itemView.getBottom());
                icon.draw(c);

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouch);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);

    }

    public void deleteCrime(UUID crimeId){
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime.getmId());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mDeleteCallBack = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem= menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }
        else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallback.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount =crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format,crimeCount);
        if(!mSubtitleVisible){
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI(){
        CrimeLab crimeLab=CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if(mAdapter==null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }
        else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private abstract class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int layout){
            super(inflater.inflate(layout,parent,false));

            itemView.setOnClickListener(this);
            mTitleTextView=(TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView=(TextView) itemView.findViewById(R.id.crime_date);
        }


        public void bind(Crime crime){
            mCrime=crime;
            mTitleTextView.setText(mCrime.getmTitle());
            mDateTextView.setText(mCrime.getmDate().toString());
        }

        @Override
        public void onClick(View v) {
           mCallback.onCrimeSelected(mCrime);
        }
    }

    private class RegularCrimeHolder extends CrimeHolder{

        public RegularCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime);
        }
    }

    private class SeriousCrimeHolder extends CrimeHolder{


        public SeriousCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.list_item_crime_serius);
        }

    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{
        private List<Crime> mCrimes;
        public CrimeAdapter(List<Crime> crimes){
            mCrimes=crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater= LayoutInflater.from(getActivity());


            if (viewType == 1) {
                return new SeriousCrimeHolder(layoutInflater, parent);
            }
            else {

                return new RegularCrimeHolder(layoutInflater, parent);
            }
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime=mCrimes.get(position);
            holder.bind(crime);

        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if (mCrimes.get(position).getmRequiresPolice()) {
                return 1;
            }
            return 0;
        }
    }
}
