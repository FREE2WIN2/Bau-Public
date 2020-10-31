package net.wargearworld.bau.communication;

import net.wargearworld.bau.hikariCP.DBConnection;
import net.wargearworld.db.model.PluginCommunication;
import net.wargearworld.db.model.PluginCommunication_;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Stack;



public class DatabaseCommunication {

    private static final String NAME_OF_ME = "Bau";
    public static int repeatDelay = 5 * 20; // 5Sekunden Delay

    public static void sendMessage(String reciever, String subchannel, String command) {
        EntityManager em = CDI.current().select(EntityManager.class).get();
        PluginCommunication communication = new PluginCommunication();
        communication.setCommand(command);
        communication.setReceiver(reciever);
        communication.setSubChannel(subchannel);
        communication.setSender(NAME_OF_ME);
        em.getTransaction().begin();
        em.persist(communication);
        em.getTransaction().commit();
    }

    public static Stack<PluginMessage> readMessages() {
        Stack<PluginMessage> out = new Stack<>();

        EntityManager em = CDI.current().select(EntityManager.class).get();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<PluginCommunication> criteriaQuery = criteriaBuilder.createQuery(PluginCommunication.class);
        Root<PluginCommunication> root = criteriaQuery.from(PluginCommunication.class);

        criteriaQuery.where(criteriaBuilder.notEqual(root.get(PluginCommunication_.sender), NAME_OF_ME));
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(PluginCommunication_.id.getName())));

        Query query = em.createQuery(criteriaQuery);
        List<PluginCommunication> list = query.getResultList();

        for (PluginCommunication communication : list) {
            out.add(new PluginMessage(communication));
        }
        return out;
    }

    public static void deleteMessage(Long id) {
        EntityManager em = CDI.current().select(EntityManager.class).get();
        PluginCommunication communication = CDI.current().select(EntityManager.class).get().find(PluginCommunication.class, id);
        em.getTransaction().begin();
        em.remove(communication);
        em.getTransaction().commit();
    }

    public static void startRecieve() {
		/*
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), ()->{
			
				for (PluginMessage message : readMessages()) {
					message.operate();
				}
		}, 0, repeatDelay);*/
    }

    public static void sendACK(String receiever, String subChannel, String command) {
        sendMessage(receiever, subChannel, command);
    }


}
