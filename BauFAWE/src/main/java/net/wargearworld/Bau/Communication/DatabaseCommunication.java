package net.wargearworld.Bau.Communication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

import net.wargearworld.Bau.HikariCP.DBConnection;
import net.wargearworld.db.model.Plot;
import net.wargearworld.db.model.Plot_;
import net.wargearworld.db.model.PluginCommunication;
import net.wargearworld.db.model.PluginCommunication_;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.Bukkit;

import net.wargearworld.Bau.Main;
import org.hibernate.criterion.Order;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;


public class DatabaseCommunication {

	private static String nameOfMe = "Bau";
	public static int repeatDelay = 5*20; // 5Sekunden Delay
	
	public static void sendMessage(String reciever, String subchannel, String command) {
		PluginCommunication communication = new PluginCommunication();
		communication.setCommand(command);
		communication.setReceiver(reciever);
		communication.setSubChannel(subchannel);
		communication.setSender(nameOfMe);
		DBConnection.persist(communication);
	}

	public static Stack<PluginMessage> readMessages() {
		Stack<PluginMessage> out = new Stack<>();

		EntityManager em = DependencyProvider.getEntityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PluginCommunication> criteriaQuery = criteriaBuilder.createQuery(PluginCommunication.class);
		Root root = criteriaQuery.from(PluginCommunication.class);

		criteriaQuery.where(criteriaBuilder.notEqual(root.get(PluginCommunication_.sender),nameOfMe));
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(PluginCommunication_.id.getName())));

		Query query = em.createQuery(criteriaQuery);
		List<PluginCommunication> list = query.getResultList();

		for(PluginCommunication communication:list){
			out.add(new PluginMessage(communication));
		}
		return out;
	}

	public static void deleteMessage(Long id) {
		PluginCommunication communication = DependencyProvider.getEntityManager().find(PluginCommunication.class,id);
		DBConnection.remove(communication);
	}
	
	public static void startRecieve() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				for (PluginMessage message : readMessages()) {
					message.operate();
				}
			}
		}, 0, repeatDelay);
	}

	public static void sendACK(String receiever, String subChannel, String command) {
		sendMessage(receiever, subChannel, command);	
	}
	
	
}
