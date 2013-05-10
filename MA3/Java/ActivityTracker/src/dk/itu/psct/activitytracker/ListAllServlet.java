package dk.itu.psct.activitytracker;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class ListAllServlet extends HttpServlet {
	private static final long serialVersionUID = -3784691473633836617L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
  		resp.setContentType("text/plain");
  		
  		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  		Query q = new Query("Recording");
  		Iterable<Entity> entities = datastore.prepare(q).asIterable();
  		HashSet<String> recordings = new HashSet<String>();
  		for (Entity r : entities)
  		{
  			recordings.add(r.getParent().getName());
  		}
  		for (String s : recordings)
  		{
  			resp.getWriter().println(s);
  		}
  		
  		
  		resp.getWriter().flush();
	}
}
