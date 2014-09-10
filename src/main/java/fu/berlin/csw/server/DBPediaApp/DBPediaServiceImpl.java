package fu.berlin.csw.server.DBPediaApp;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.bmir.protege.web.server.WebProtegeRemoteServiceServlet;
import edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import fu.berlin.csw.DBPediaApp.client.rpc.DBPediaService;
import fu.berlin.csw.DBPediaApp.client.ui.DBPediaPortlet.Message;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 21/08/2014
 */

public class DBPediaServiceImpl extends WebProtegeRemoteServiceServlet
		implements DBPediaService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Set<ProjectChangeXMLBuilder> projectChangeXMLBuilders;
	private Message message;

	public DBPediaServiceImpl() {
		projectChangeXMLBuilders = new HashSet<ProjectChangeXMLBuilder>();

	}

	@Override
	public Message getMessage(ProjectId projectId) {
		try {

			UserId currentUser = this.getUserInSession();

			message = new Message();

			for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
				if (projectId.equals(builder.getProjectId())) {
					int changeCount = builder.getChangeCount();

					message.setMessage(builder.getXMLasString(currentUser)
							+ "\n\nUser: "
							+ this.getUserInSession().getUserName()
							+ " ChangeCount: " + changeCount);
				}
			}

			return message;

		} catch (Exception e) {
			message.setMessage(e.getMessage());
			return message;
		}

	}

	@Override
	public void init(ProjectId projectId) {

		for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
			if (builder.getProjectId().equals(projectId)) {
				return;
			}
		}
		ProjectChangeXMLBuilder new_builder = new ProjectChangeXMLBuilder(
				projectId);
		projectChangeXMLBuilders.add(new_builder);

	}

	@Override
	public void postChangeEvent(ProjectId projectId, ProjectChangedEvent event) {

		for (ProjectChangeXMLBuilder builder : projectChangeXMLBuilders) {
			if (builder.getProjectId().equals(event.getProjectId())) {

				if (event.getRevisionNumber().getValue() <= builder
						.getRevisionNumber().getValue()) {
					return;
				}
				builder.setRevisionNumber(event.getRevisionNumber());

				builder.addChange(event.getSubjects(), event.getUserId());
				return;
			}
		}

	}

}
