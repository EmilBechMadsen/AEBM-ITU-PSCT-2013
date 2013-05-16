package dk.itu.psct.activitytracker;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class DeleteEntitiesServlet extends HttpServlet {
	private static final long serialVersionUID = -3784691473633836617L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
  		resp.setContentType("text/plain");
  		
  		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  		Query q = new Query("Recording");
  		Iterable<Entity> entities = datastore.prepare(q).asIterable();
  		for (Entity e : entities)
  		{
  			datastore.delete(e.getKey());
  		}
  		
  		resp.getWriter().write("All records deleted.");
  		resp.getWriter().flush();
	}
}
