
package derms.replica1.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swapResource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="swapResource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="coordinatorID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oldResourceID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="oldResourceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newResourceID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newResourceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "swapResource", propOrder = {
    "coordinatorID",
    "oldResourceID",
    "oldResourceType",
    "newResourceID",
    "newResourceType"
})
public class SwapResource {

    protected String coordinatorID;
    protected String oldResourceID;
    protected String oldResourceType;
    protected String newResourceID;
    protected String newResourceType;

    /**
     * Gets the value of the coordinatorID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoordinatorID() {
        return coordinatorID;
    }

    /**
     * Sets the value of the coordinatorID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoordinatorID(String value) {
        this.coordinatorID = value;
    }

    /**
     * Gets the value of the oldResourceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldResourceID() {
        return oldResourceID;
    }

    /**
     * Sets the value of the oldResourceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldResourceID(String value) {
        this.oldResourceID = value;
    }

    /**
     * Gets the value of the oldResourceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldResourceType() {
        return oldResourceType;
    }

    /**
     * Sets the value of the oldResourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldResourceType(String value) {
        this.oldResourceType = value;
    }

    /**
     * Gets the value of the newResourceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewResourceID() {
        return newResourceID;
    }

    /**
     * Sets the value of the newResourceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewResourceID(String value) {
        this.newResourceID = value;
    }

    /**
     * Gets the value of the newResourceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewResourceType() {
        return newResourceType;
    }

    /**
     * Sets the value of the newResourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewResourceType(String value) {
        this.newResourceType = value;
    }

}
