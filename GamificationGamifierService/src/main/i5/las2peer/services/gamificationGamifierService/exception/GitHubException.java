package i5.las2peer.services.gamificationGamifierService.exception;

/**
 * 
 * Exception thrown when something went wrong during GitHub access.
 *
 */
public class GitHubException extends Exception {

  private static final long serialVersionUID = -1622464573552868191L;

  public GitHubException(String message) {
    super(message);
  }
}
