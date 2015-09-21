package li.itcc.hackathon15.util.loading;

/**
 * Created by Arthur on 19.09.2015.
 */
public interface TaskExecutionListener<RESULT> extends TaskProgressListener, TaskAbortListener, TaskCompletionListener<RESULT> {

}
