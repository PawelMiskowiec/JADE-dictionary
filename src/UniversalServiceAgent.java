package jadelab1;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.net.*;
import java.io.*;

public class UniversalServiceAgent extends Agent {
        protected void setup () {
            //services registration at DF
            DFAgentDescription dfad = new DFAgentDescription();
            dfad.setName(getAID());
            //service no 4
            ServiceDescription sd4 = new ServiceDescription();
            sd4.setType("answers");
            sd4.setName("universal");

            //add them all
            dfad.addServices(sd4);


            try {
                DFService.register(this,dfad);
            } catch (FIPAException ex) {
                ex.printStackTrace();
            }

            addBehaviour(new UniversalCyclicBehaviour(this));
            //doDelete();
        }
        protected void takeDown() {
            //services deregistration before termination
            try {
                DFService.deregister(this);
            } catch (FIPAException ex) {
                ex.printStackTrace();
            }
        }
        public String makeRequest(String serviceName, String word)
        {
            StringBuffer response = new StringBuffer();
            try
            {
                URL url;
                URLConnection urlConn;
                DataOutputStream printout;
                DataInputStream input;
                url = new URL("http://dict.org/bin/Dict");
                urlConn = url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(serviceName) + "&Query=" + URLEncoder.encode(word) + "&submit=Submit+query";
                //forth
                printout = new DataOutputStream(urlConn.getOutputStream());
                printout.writeBytes(content);
                printout.flush();
                printout.close();
                //back
                input = new DataInputStream(urlConn.getInputStream());
                String str;
                while (null != ((str = input.readLine())))
                {
                    response.append(str);
                }
                input.close();
            }
            catch (Exception ex)
            {
                System.out.println(ex.getMessage());
            }
            //cut what is unnecessary
            return response.substring(response.indexOf("<hr>")+4, response.lastIndexOf("<hr>"));
        }

    }

    class UniversalCyclicBehaviour extends CyclicBehaviour
    {
        UniversalServiceAgent agent;
        public UniversalCyclicBehaviour(UniversalServiceAgent agent)
        {
            this.agent = agent;
        }
        public void action()
        {
            ACLMessage message = agent.receive();
            if (message == null)
            {
                block();
            }
            else
            {
                //process the incoming message
                String content = message.getContent();
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                String response = "";
                try
                {
                    // done
                    response = agent.makeRequest(message.getOntology(), content);
                }
                catch (NumberFormatException ex)
                {
                    response = ex.getMessage();
                }
                reply.setContent(response);
                agent.send(reply);
            }
        }
    }

