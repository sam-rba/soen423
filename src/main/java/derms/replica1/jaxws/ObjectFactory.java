
package derms.replica1.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the derms.replica1.jaxws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RemoveResourceResponse_QNAME = new QName("http://replica1.derms/", "removeResourceResponse");
    private final static QName _SwapResourceResponse_QNAME = new QName("http://replica1.derms/", "swapResourceResponse");
    private final static QName _AddResource_QNAME = new QName("http://replica1.derms/", "addResource");
    private final static QName _ReturnResourceResponse_QNAME = new QName("http://replica1.derms/", "returnResourceResponse");
    private final static QName _RequestResourceResponse_QNAME = new QName("http://replica1.derms/", "requestResourceResponse");
    private final static QName _ListResourceAvailability_QNAME = new QName("http://replica1.derms/", "listResourceAvailability");
    private final static QName _RemoveResource_QNAME = new QName("http://replica1.derms/", "removeResource");
    private final static QName _AddResourceResponse_QNAME = new QName("http://replica1.derms/", "addResourceResponse");
    private final static QName _FindResourceResponse_QNAME = new QName("http://replica1.derms/", "findResourceResponse");
    private final static QName _SwapResource_QNAME = new QName("http://replica1.derms/", "swapResource");
    private final static QName _ListResourceAvailabilityResponse_QNAME = new QName("http://replica1.derms/", "listResourceAvailabilityResponse");
    private final static QName _FindResource_QNAME = new QName("http://replica1.derms/", "findResource");
    private final static QName _ReturnResource_QNAME = new QName("http://replica1.derms/", "returnResource");
    private final static QName _RequestResource_QNAME = new QName("http://replica1.derms/", "requestResource");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: derms.replica1.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RequestResource }
     * 
     */
    public RequestResource createRequestResource() {
        return new RequestResource();
    }

    /**
     * Create an instance of {@link FindResource }
     * 
     */
    public FindResource createFindResource() {
        return new FindResource();
    }

    /**
     * Create an instance of {@link ReturnResource }
     * 
     */
    public ReturnResource createReturnResource() {
        return new ReturnResource();
    }

    /**
     * Create an instance of {@link ListResourceAvailabilityResponse }
     * 
     */
    public ListResourceAvailabilityResponse createListResourceAvailabilityResponse() {
        return new ListResourceAvailabilityResponse();
    }

    /**
     * Create an instance of {@link SwapResource }
     * 
     */
    public SwapResource createSwapResource() {
        return new SwapResource();
    }

    /**
     * Create an instance of {@link FindResourceResponse }
     * 
     */
    public FindResourceResponse createFindResourceResponse() {
        return new FindResourceResponse();
    }

    /**
     * Create an instance of {@link RemoveResource }
     * 
     */
    public RemoveResource createRemoveResource() {
        return new RemoveResource();
    }

    /**
     * Create an instance of {@link AddResourceResponse }
     * 
     */
    public AddResourceResponse createAddResourceResponse() {
        return new AddResourceResponse();
    }

    /**
     * Create an instance of {@link ListResourceAvailability }
     * 
     */
    public ListResourceAvailability createListResourceAvailability() {
        return new ListResourceAvailability();
    }

    /**
     * Create an instance of {@link AddResource }
     * 
     */
    public AddResource createAddResource() {
        return new AddResource();
    }

    /**
     * Create an instance of {@link ReturnResourceResponse }
     * 
     */
    public ReturnResourceResponse createReturnResourceResponse() {
        return new ReturnResourceResponse();
    }

    /**
     * Create an instance of {@link RequestResourceResponse }
     * 
     */
    public RequestResourceResponse createRequestResourceResponse() {
        return new RequestResourceResponse();
    }

    /**
     * Create an instance of {@link SwapResourceResponse }
     * 
     */
    public SwapResourceResponse createSwapResourceResponse() {
        return new SwapResourceResponse();
    }

    /**
     * Create an instance of {@link RemoveResourceResponse }
     * 
     */
    public RemoveResourceResponse createRemoveResourceResponse() {
        return new RemoveResourceResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "removeResourceResponse")
    public JAXBElement<RemoveResourceResponse> createRemoveResourceResponse(RemoveResourceResponse value) {
        return new JAXBElement<RemoveResourceResponse>(_RemoveResourceResponse_QNAME, RemoveResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SwapResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "swapResourceResponse")
    public JAXBElement<SwapResourceResponse> createSwapResourceResponse(SwapResourceResponse value) {
        return new JAXBElement<SwapResourceResponse>(_SwapResourceResponse_QNAME, SwapResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "addResource")
    public JAXBElement<AddResource> createAddResource(AddResource value) {
        return new JAXBElement<AddResource>(_AddResource_QNAME, AddResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "returnResourceResponse")
    public JAXBElement<ReturnResourceResponse> createReturnResourceResponse(ReturnResourceResponse value) {
        return new JAXBElement<ReturnResourceResponse>(_ReturnResourceResponse_QNAME, ReturnResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "requestResourceResponse")
    public JAXBElement<RequestResourceResponse> createRequestResourceResponse(RequestResourceResponse value) {
        return new JAXBElement<RequestResourceResponse>(_RequestResourceResponse_QNAME, RequestResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListResourceAvailability }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "listResourceAvailability")
    public JAXBElement<ListResourceAvailability> createListResourceAvailability(ListResourceAvailability value) {
        return new JAXBElement<ListResourceAvailability>(_ListResourceAvailability_QNAME, ListResourceAvailability.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "removeResource")
    public JAXBElement<RemoveResource> createRemoveResource(RemoveResource value) {
        return new JAXBElement<RemoveResource>(_RemoveResource_QNAME, RemoveResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "addResourceResponse")
    public JAXBElement<AddResourceResponse> createAddResourceResponse(AddResourceResponse value) {
        return new JAXBElement<AddResourceResponse>(_AddResourceResponse_QNAME, AddResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindResourceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "findResourceResponse")
    public JAXBElement<FindResourceResponse> createFindResourceResponse(FindResourceResponse value) {
        return new JAXBElement<FindResourceResponse>(_FindResourceResponse_QNAME, FindResourceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SwapResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "swapResource")
    public JAXBElement<SwapResource> createSwapResource(SwapResource value) {
        return new JAXBElement<SwapResource>(_SwapResource_QNAME, SwapResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListResourceAvailabilityResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "listResourceAvailabilityResponse")
    public JAXBElement<ListResourceAvailabilityResponse> createListResourceAvailabilityResponse(ListResourceAvailabilityResponse value) {
        return new JAXBElement<ListResourceAvailabilityResponse>(_ListResourceAvailabilityResponse_QNAME, ListResourceAvailabilityResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "findResource")
    public JAXBElement<FindResource> createFindResource(FindResource value) {
        return new JAXBElement<FindResource>(_FindResource_QNAME, FindResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "returnResource")
    public JAXBElement<ReturnResource> createReturnResource(ReturnResource value) {
        return new JAXBElement<ReturnResource>(_ReturnResource_QNAME, ReturnResource.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://replica1.derms/", name = "requestResource")
    public JAXBElement<RequestResource> createRequestResource(RequestResource value) {
        return new JAXBElement<RequestResource>(_RequestResource_QNAME, RequestResource.class, null, value);
    }

}
