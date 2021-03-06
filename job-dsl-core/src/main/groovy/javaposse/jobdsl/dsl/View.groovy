package javaposse.jobdsl.dsl

/**
 * DSL element representing a Jenkins view.
 */
abstract class View implements Context {
    private final List<WithXmlAction> withXmlActions = []

    protected final JobManagement jobManagement

    String name

    protected View(JobManagement jobManagement) {
        this.jobManagement = jobManagement
    }

    @Deprecated
    void name(String name) {
        jobManagement.logDeprecationWarning()
        this.name = name
    }

    void description(String descriptionArg) {
        execute {
            it / delegate.methodMissing('description', descriptionArg)
        }
    }

    void filterBuildQueue(boolean filterBuildQueueArg = true) {
        execute {
            it / delegate.methodMissing('filterQueue', filterBuildQueueArg)
        }
    }

    void filterExecutors(boolean filterExecutorsArg = true) {
        execute {
            it / delegate.methodMissing('filterExecutors', filterExecutorsArg)
        }
    }

    void configure(Closure withXmlClosure) {
        withXmlActions.add(new WithXmlAction(withXmlClosure))
    }

    /**
     * Postpone all xml processing until someone actually asks for the xml. That lets us execute everything in order,
     * even if the user didn't specify them in order.
     * @return
     */
    String getXml() {
        Writer xmlOutput = new StringWriter()
        XmlNodePrinter xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlOutput), '    ')
        xmlNodePrinter.with {
            preserveWhitespace = true
            expandEmptyElements = true
            quote = "'" // Use single quote for attributes
        }
        xmlNodePrinter.print(node)

        xmlOutput.toString()
    }

    Node getNode() {
        Node root = new XmlParser().parse(new StringReader(template))

        withXmlActions.each { it.execute(root) }
        root
    }

    protected void execute(Closure rootClosure) {
        withXmlActions << new WithXmlAction(rootClosure)
    }

    protected abstract String getTemplate()
}
