package messages;

public class ResultMessages {
    public static final String MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL = "The music band has been added successful \n";
    public static final String ADDITION_MISTAKE = "The music band hasn't been added. Such name of the band exists already \n";
    public static final String COLLECTION_IS_EMPTY = "The collection is empty \n";
    public static final String NO_HAVE_BANDS = "You don't have added bands \n";
    public static final String NO_SUCH_ID = "The music band hasn't been updated because of isn't such id \n";
    public static final String NO_SUCH_ELEMENTS = "There's not such elements \n";
    public static final String MUSIC_BAND_HAS_BEEN_UPDATED_SUCCESSFUL = "The music band has been updated successful \n";
    public static final String MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL = "The music band has been deleted successful \n";
    public static final String MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID = "The music band has been deleted successful, id = ";
    public static final String DELETED_MISTAKE = "The music band hasn't been deleted, please, make sure that such id is exists \n";
    public static final String COLLECTION_HAS_BEEN_DELETED = "The music bands have been deleted successful \n";
    public static final String SALES_BIGGER_THAN_MIN_ELEMENT = "The sales of the entered of the best album should be less than the min sales \n";
    public static final String ALL_ELEMENT_BIGGER = "The all music bands has sales bigger than the entered \n";
    public static final String INCORRECT_ARGUMENT = "You have used an incorrect argument for this command \n";
    public static final String NO_SUCH_COMMAND = "There's no such command, " +
            "enter 'help' for see the list of available command \n";
    public static final String HISTORY_IS_EMPTY = "The command history is empty \n";
    public static final String HISTORY_CLEARED = "The command history has cleaned successful \n";

    public static final String LIST_OF_AVAILABLE_COMMAND = """
            The list of available command:\s
            - help: show the list of available commands\s
            - info: show the information about music bands\s
            - show: show everything music bands\s
            - show_mine : show only your music bands \s
            - add: add a music band\s
            - update <id> : update the music band by its id\s
            - remove <id> : remove the music band by its id\s
            - clear: delete the all music bands\s
            - exit: disconnect from the server\s
            - add_if_min: add a music band if its sales less than the min sales of the collection\s
            - remove_lower <count_sales>: delete the all music bands which have the sales less than will entered\s
            - history: show last 12 commands\s
            - history_clear: clear the command history\s
            - min_by_best_album: show any music band who have min sales of the best album\s
            - filter_by_best_album <count_sales>: show the all music bands who have the sales of the best album equals will entered\s
            - print_field_asc_best_album: show the all music bands in ascending order of sales of the best album\s
            - execute_script : run a script from a file \s
            
            -q use for an interrupt of the input data \s
            """;
}
