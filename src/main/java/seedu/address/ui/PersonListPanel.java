package seedu.address.ui;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";

    @FXML
    private ListView<Person> personListView;
    private Consumer<Person> selectionHandler = unused -> { };

    /**
     * Creates a panel that displays the given list of clients and reports selection changes.
     *
     * @param personList the clients to display
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((unused, oldPerson, newPerson) ->
                selectionHandler.accept(newPerson));
    }

    /**
     * Sets the handler notified when the selected client changes.
     *
     * @param selectionHandler the handler that receives the selected client, or {@code null} when none is selected
     */
    public void setSelectionHandler(Consumer<Person> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    /** Selects the first displayed client, or clears the selection and notifies the handler when the list is empty. */
    void selectFirst() {
        if (personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().clearSelection();
            selectionHandler.accept(null);
        } else {
            personListView.getSelectionModel().selectFirst();
            selectionHandler.accept(personListView.getItems().getFirst());
        }
    }

    /** Returns the currently selected client, or {@code null} when no client is selected. */
    public Person getSelectedPerson() {
        return personListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Applies a command's selection request after the displayed list changes, then notifies the selection handler.
     * Selects the first client if the requested or previously selected client is absent.
     *
     * @param result the command result containing the selection request
     * @param previouslySelected the client selected before the displayed list changed
     */
    public void applySelection(CommandResult result, Person previouslySelected) {
        ObservableList<Person> displayed = personListView.getItems();
        int index = indexToSelect(displayed, result, previouslySelected);
        if (index < 0) {
            selectFirst();
            return;
        }
        Person selected = displayed.get(index);
        personListView.getSelectionModel().select(index);
        personListView.scrollTo(index);
        // Replacing a client can retain the same row without firing the selection listener.
        selectionHandler.accept(selected);
    }

    /**
     * Finds the requested client, or the previously selected client, in display order.
     * Returns the first row if no client matches, or {@code -1} if the list is empty.
     */
    static int indexToSelect(List<Person> displayed, CommandResult result, Person previouslySelected) {
        Person desired = result.getSelectionRequest() == CommandResult.SelectionRequest.SELECT_PERSON
                ? result.getPersonToSelect() : previouslySelected;
        ClientReference reference = result.getSelectionRequest() == CommandResult.SelectionRequest.SELECT_REFERENCE
                ? result.getSelectedClientReference().orElseThrow() : null;
        for (int i = 0; i < displayed.size(); i++) {
            Person candidate = displayed.get(i);
            if (reference != null && reference.equals(candidate.getClientReference())
                    || reference == null && desired != null && desired.isSamePerson(candidate)) {
                return i;
            }
        }
        return displayed.isEmpty() ? -1 : 0;
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setText(null);
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
