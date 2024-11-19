package messages;

public class ResultMessages {
    public static final String MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL = "The music bang was successfully added with id = ";
    public static final String ADDITION_MISTAKE = "The music band has not been added. Such name of the band exists already \n";
    public static final String COLLECTION_IS_EMPTY = "The collection is empty \n";
    public static final String NO_HAVE_BANDS = "You do not have added bands \n";
    public static final String NO_SUCH_ID = "The music band has not been updated because of is not such id \n";
    public static final String NO_SUCH_ELEMENTS = "There is not such elements \n";
    public static final String MUSIC_BAND_HAS_BEEN_UPDATED_SUCCESSFUL = "The music band has been updated successful \n";
    public static final String MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL = "The music band has been deleted successful \n";
    public static final String MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID = "The music band has been deleted successful, id = ";
    public static final String DELETED_MISTAKE = "The music band has not been deleted, please, make sure that such id is exists \n";
    public static final String COLLECTION_HAS_BEEN_DELETED = "The music bands have been deleted successful \n";
    public static final String ALL_ELEMENT_BIGGER = "The all the music bands have sales numbers higher than what you put in \n";
    public static final String INCORRECT_ARGUMENT = "You have used an incorrect argument for this command \n";
    public static final String NO_SUCH_COMMAND = "There is no such command, " +
            "enter 'help' to see the list of available command \n";
    public static final String HISTORY_IS_EMPTY = "The command history is empty \n";
    public static final String HISTORY_CLEARED = "The command history has cleaned successful \n";

    public static final String LIST_OF_AVAILABLE_COMMAND = """
            The list of available command:\s
            - help: show the list of available commands\s
            - info: show the information about the all music bands\s
            - show: show all music bands\s
            - show_mine : show your music bands \s
            - show_min: show a music band which has min sales of the best album\s
            - print_asc: show the all music bands in ascending order of the sales of the best album\s
            - print_desc: show the all music bands in descending order of the sales ot the best album\s
            - filter <count_sales>: show the all music bands which have the sales of the best album equals will entered\s
            - add: add the music band\s
            - add_if_min: add a music band if the number of sales of its best album is less than your existing albums\s
            - update <id> : update your music band by its id\s
            - history: show last 12 commands\s
            - history_clear: clear the command history\s
            - remove <id> : remove your music band by its id\s  
            - remove_lower <count_sales>: delete all your music bands which have the sales less than will entered\s
            - clear: delete all your music bands\s
            - execute_script : run a script from a file \s
            - exit: disconnect from the server\s
            
            - q use for an interrupt of the input data \s
            """;
}
