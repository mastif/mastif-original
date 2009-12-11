/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfact;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.mitre.medfact.xml.ClassRefType;
import org.mitre.medfact.xml.ClassType;
import org.mitre.medfact.xml.ConfigurationType;
import org.mitre.medfact.xml.DirectionType;
import org.mitre.medfact.xml.RuleRefType;
import org.mitre.medfact.xml.RuleType;
import org.mitre.medfact.xml.TagType;

/**
 *
 * @author MCOARR
 */
public class MedFactsXml
{

    public void debugPrintList(String listName, Collection currentRuleContentList) {
        System.out.format("%s:%n", listName);
        if (currentRuleContentList == null) {
            System.out.format("%s is null", listName);
        } else if (currentRuleContentList.isEmpty()) {
            System.out.format("%s is empty", listName);
        } else {
            for (Object current : currentRuleContentList) {
                System.out.format(" - %s%n", current.toString());
            }
        }
    }
    public void execute()
    {
        String filename = "rules_sample_09a.xml";
        URL resourceUrl = getClass().getClassLoader().getResource(filename);
        System.out.format("file url: %s%n", resourceUrl.toString());
        try {
            Source inputSource = new StreamSource(resourceUrl.openStream());

            JAXBContext context = JAXBContext.newInstance("org.mitre.medfact.xml");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ConfigurationType> jaxbElement =
                    unmarshaller.unmarshal(inputSource, ConfigurationType.class);
            
            ConfigurationType configurationObject = jaxbElement.getValue();
            System.out.format("configuration object: %s%n", configurationObject);

            List<List<Serializable>> masterRuleList = new ArrayList<List<Serializable>>();

            for (RuleType currentRule : configurationObject.getRule())
            {
                Deque<ContentAndPosition> replacements = new ArrayDeque<ContentAndPosition>();
                String id = currentRule.getId();
                DirectionType direction = currentRule.getDirection();
                TagType tag = currentRule.getTag();
                List<Serializable> content = currentRule.getContent();

                System.out.format("id: %s%n", id);
                System.out.format("direction: %s%n", direction.value());
                System.out.format("tag: %s%n", tag.value());
                System.out.format("content: %n");
                int currentPosition = 0;
                for (Serializable currentContent : content)
                {
                    String contentString = currentContent.toString();
                    Class contentClass = currentContent.getClass();
                    System.out.format(" - ");
                    System.out.format("(currentContent.getClass(): %s)", currentContent.getClass().getName());
                    if (currentContent instanceof JAXBElement)
                    {
                        JAXBElement genericElement = (JAXBElement)currentContent;
                        System.out.format("(JAXBElement)");
                        Class declaredType = genericElement.getDeclaredType();
                        if (RuleRefType.class.isAssignableFrom(declaredType))
                        {
                            RuleRefType currentRuleRef = (RuleRefType)genericElement.getValue();
                            Object idRefObject = currentRuleRef.getIdref();
                            JAXBElement<RuleType> referencedElement = (JAXBElement<RuleType>)idRefObject;
                            RuleType referencedRule = referencedElement.getValue();
                            System.out.format("[ruleref]");
                            System.out.format("[idref class: %s; idref value: %s]", idRefObject.getClass(), idRefObject.toString());
                            ContentAndPosition contentAndPosition = new ContentAndPosition(genericElement, currentPosition);
                            replacements.push(contentAndPosition);

                        } else if (ClassRefType.class.isAssignableFrom(declaredType))
                        {
                            ClassRefType currentClassRef = (ClassRefType)genericElement.getValue();
                            Object idRefObject = currentClassRef.getIdref();
                            //System.out.format("%n[idref object's class=%s]%n", idRefObject.getClass());
                            //JAXBElement<ClassType> referencedElement = (JAXBElement<ClassType>)idRefObject;
                            //ClassType referencedClass = referencedElement.getValue();
                            ClassType referencedClass = (ClassType)idRefObject;
                            System.out.format("[classref]");
                            System.out.format("[idref class: %s; idref value: %s]", idRefObject.getClass(), idRefObject.toString());

                            ContentAndPosition contentAndPosition = new ContentAndPosition(genericElement, currentPosition);
                            replacements.push(contentAndPosition);
                        }
                        System.out.format("(jaxbelement's declared type: %s)", declaredType.getName());
                    }
                    System.out.format("\"%s\"%n",contentString);

                    currentPosition++;
                }

                List<List<Serializable>> currentResultList = resolveReplacements(currentRule.getContent(), replacements);
                masterRuleList.addAll(currentResultList);
                System.out.format("end content.%n");

            }

            System.out.println("=== MASTER LIST BEGIN ===");
            for (List<Serializable> currentRule : masterRuleList)
            {
                System.out.format("    - ");
                for (Serializable currentRulePart : currentRule)
                {
                    System.out.format("\"%s\"; ", currentRulePart.toString());
                }
                System.out.println();
            }
            System.out.println("===  MASTER LIST END  ===");
        } catch (IOException ex) {
            Logger.getLogger(MedFactsXml.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (JAXBException ex) {
            Logger.getLogger(MedFactsXml.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        MedFactsXml medFactsXml = new MedFactsXml();
        medFactsXml.execute();
    }

    private List<List<Serializable>> resolveReplacements(List<Serializable> currentRuleContentList, Deque<ContentAndPosition> replacements)
    {
        System.out.println("BEGIN DEBUG resolveReplacements");
        debugPrintList("currentRuleContentList", currentRuleContentList);
        debugPrintList("replacements", replacements);
        System.out.println("END DEBUG resolveReplacements");
        if (replacements.isEmpty())
        {
            List<List<Serializable>> returnedList = new ArrayList<List<Serializable>>();
            returnedList.add(currentRuleContentList);
            return returnedList;
        }

        ContentAndPosition currentContentAndPosition = replacements.pop();
        int currentPosition = currentContentAndPosition.getPosition();
        JAXBElement currentContent = currentContentAndPosition.getContent();

        List returnedList = new ArrayList<List<Serializable>>();

        List leftList = new ArrayList<Serializable>();
        List rightList = new ArrayList<Serializable>();

        System.out.format("currentPosition: %d%n", currentPosition);

        for (int i=0; i < currentPosition; i++)
        {
            Serializable c = currentRuleContentList.get(i);
            leftList.add(c);
        }
        debugPrintList("leftList", leftList);

        for (int j = currentPosition + 1; j < currentRuleContentList.size(); j++)
        {
            Serializable c = currentRuleContentList.get(j);
            rightList.add(c);
        }
        debugPrintList("rightList", rightList);

        if (currentContent.getDeclaredType().equals(ClassRefType.class))
        {
            JAXBElement<ClassRefType> currentElement = (JAXBElement<ClassRefType>)currentContent;

            ClassRefType currentClassRef = currentElement.getValue();
            Object idRefObject = currentClassRef.getIdref();
//            JAXBElement<ClassType> referencedElement = (JAXBElement<ClassType>)idRefObject;
//            ClassType referencedClass = referencedElement.getValue();
            ClassType referencedClass = (ClassType)idRefObject;

            List<String> classValueList = referencedClass.getClassValue();
            for (String currentClassValue : classValueList)
            {
                List newList = new ArrayList<Serializable>();
                newList.addAll(leftList);

                newList.add(currentClassValue);

                newList.addAll(rightList);
                returnedList.add(newList);
            }
        }

        return returnedList;



//        if (replacements.isEmpty())
//        {
//
//        } else
//        {
//            Deque<ContentAndPosition> smallerReplacements = new ArrayDeque<ContentAndPosition>();
//            smallerReplacements.addAll(replacements);
//            smallerReplacements.pop();
//            return resolveReplacements(smallerReplacements);
//        }
    }

    public class ContentAndPosition
    {
        private int position;
        private JAXBElement content;

        public String toString()
        {
            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            printer.format("position: %d;", position);
            printer.format("element class: %s;", content.getClass().getName());
            printer.format("element content class: %s;", content.getDeclaredType().getName());
            printer.format("element content: %s;;", content.getValue());

            return writer.toString();
        }

        public ContentAndPosition()
        {
        }

        public ContentAndPosition(JAXBElement content, int position)
        {
            this.content = content;
            this.position = position;
        }

        /**
         * @return the position
         */
        public int getPosition() {
            return position;
        }

        /**
         * @param position the position to set
         */
        public void setPosition(int position) {
            this.position = position;
        }

        /**
         * @return the content
         */
        public JAXBElement getContent() {
            return content;
        }

        /**
         * @param content the content to set
         */
        public void setContent(JAXBElement content) {
            this.content = content;
        }
    }


}
