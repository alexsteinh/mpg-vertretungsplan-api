/**
 * Listener for async functionality of the ReplacementTable downloader
 */
public interface OnDownloadFinishedListener
{
    void onFinished(ReplacementTable table);
    void onFailed(String message);
}
