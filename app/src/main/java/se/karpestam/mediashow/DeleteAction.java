package se.karpestam.mediashow;

import android.os.AsyncTask;

/**
 * Created by Mats on 2015-05-17.
 */
public class DeleteAction {
    private DeleteListener mDeleteListener;
    private final String[] mPathsToDelete;

    public interface DeleteListener {
        void onDeleteProgress(int deleting, int totalToDelete);

        void onDeleteFinished(boolean resultOk);
    }

    public DeleteAction(String[] pathsToDelete) {
        mPathsToDelete = pathsToDelete;
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

    private class DeleteTask extends AsyncTask<String[], Void, Boolean> {

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(String[]... strings) {
//            final int numberOfPathsToDelete = strings.length;
//            publishProgress(0, numberOfPathsToDelete);
//            for (int i = 0; i < numberOfPathsToDelete; i++) {
//
//            }
//            publishProgress();
            return null;
        }
    }
}
