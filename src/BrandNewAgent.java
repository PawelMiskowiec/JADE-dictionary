package jadelab1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.*;

public class BrandNewAgent extends Agent {
    protected void setup () {
        displayResponse("Hello, I am " + getAID().getLocalName());
        addBehaviour(new BrandNewCyclicBehaviour(this));
        //doDelete();
    }
    protected void displayResponse(String message) {
        JOptionPane.showMessageDialog(null,message,"Message",JOptionPane.PLAIN_MESSAGE);
    }
    protected void takeDown() {
        displayResponse("See you");
    }
    public void displayHtmlResponse(String html) {
        JTextPane tp = new JTextPane();
        JScrollPane js = new JScrollPane();
        js.getViewport().add(tp);
        JFrame jf = new JFrame();
        jf.getContentPane().add(js);
        jf.pack();
        jf.setSize(400,500);
        jf.setVisible(true);
        tp.setContentType("text/html");
        tp.setEditable(false);
        tp.setText(html);
    }
}

class BrandNewCyclicBehaviour extends CyclicBehaviour
{
    BrandNewAgent agent;
    AtomicLong id = new AtomicLong(0);
    Map<Long, String> wordsMap = new HashMap<>();
    public BrandNewCyclicBehaviour(BrandNewAgent agent) {
        this.agent = agent;
    }

    public void action() {
        ACLMessage message = agent.receive();
        if (message == null) {
            block();
        } else {
            String ontology = message.getOntology();
            String content = message.getContent();
            int performative = message.getPerformative();
            if (performative == ACLMessage.REQUEST)
            {
                DFAgentDescription dfad = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setName("universal");
                dfad.addServices(sd);
                try
                {
                    DFAgentDescription[] result = DFService.search(agent, dfad);
                    if (result.length == 0) agent.displayResponse("No service has been found ...");
                    else
                    {
                        String foundAgent = result[0].getName().getLocalName();
                        agent.displayResponse("Agent " + foundAgent + " is a service provider. Sending message to " + foundAgent);

                        //Keeping track of sent words
                        wordsMap.put(id.incrementAndGet(), content);

                        ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                        forward.addReceiver(new AID(foundAgent, AID.ISLOCALNAME));
                        forward.setContent(content);
                        forward.setReplyWith(id.toString());
                        forward.setOntology(ontology);
                        agent.send(forward);
                    }
                }
                catch (FIPAException ex)
                {
                    ex.printStackTrace();
                    agent.displayResponse("Problem occured while searching for a service ...");
                }
            }
            else
            {	//when it is an answer
                Long receivedId = Long.parseLong(message.getInReplyTo());
                String requestedWordHtml= "<p>Response for term: " + wordsMap.get(receivedId) + "</p>";
                agent.displayHtmlResponse(requestedWordHtml + content);
            }
        }
    }
}

