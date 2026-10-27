---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On a first run, the client list shows sample clients.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Click a client in the list to see their details, timestamps, and service note in the pane beside it.
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add r/C0001 n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/0.00` : Adds a client named `John Doe` to the Address Book.

    * `delete 3` : Deletes the 3rd contact shown in the current list.

    * `note r/C0001 s/Prefers morning appointments` : Adds or replaces that client's service note.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Commands that take no parameters (`help`, `list`, `exit`, and `clear`) reject extraneous parameters.<br>
  For example, `help 123` is invalid; trailing whitespace is accepted.
  The `sort` command checks its optional order argument and rejects any additional text.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.
Each new client needs a unique reference (`r/REFERENCE`). References are case-insensitive and are saved in uppercase.
Credit scores are nonnegative whole numbers. Debt is a nonnegative amount with at most two decimal places;
The debt is stored with two decimal places; no currency is assigned.
Card number, CVV, expiry date, and provider are required nonblank fields when adding a client. They are saved as plain text, and command input is trimmed at its edges. The client list displays the provider, expiry date, and a masked card number. A card can be removed with `removecard`.
Expiry dates must use `MM/YY`, with a month from `01` to `12` (e.g. `12/28`).
This checks the format only; past expiry dates are accepted.

Format: `add r/REFERENCE n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS cn/CARD_NUMBER cvv/CVV exp/EXPIRY_DATE provider/PROVIDER cs/CREDIT_SCORE d/DEBT [t/TAG]... `

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
</box>

Examples:
* `add r/C0001 n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/0.00`
* `add r/C0002 n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 cn/5555555555554444 cvv/456 exp/11/29 provider/Mastercard cs/725 d/150.50 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.
The active sort order, if any, is retained.

Format: `list`

### Sorting clients by name: `sort`

Sorts the displayed clients by name for the current session. Names are compared without regard to case using Unicode character values, rather than language-specific alphabetization. Accented letters sort after unaccented letters; clients with equal names are ordered by their client references. Sorting changes the displayed order only and does not save or rearrange the data file. The selected client remains selected in the details panel. `find` results and later `list` commands use the active order. Use `sort reset` to restore the stored order.

Format: `sort [asc|desc|reset]`

* `sort` or `sort asc` displays names from A to Z.
* `sort desc` displays names from Z to A. Equal names still use ascending client references.
* `sort reset` restores the original stored order.
* The order argument must be lowercase. Additional arguments are rejected.

Example: `sort desc`

### Viewing client timestamps

Select a client to see read-only `Created` and `Last Modified` times under **Profile history** in the side panel. They appear after `add`, `list`, or `find` when a client is selected; there is no timestamp command. Times use your computer's local time in `yyyy-MM-dd HH:mm` format. For example:

```text
Created:        2026-03-15 09:42
Last Modified:  2026-10-12 14:18
```

For a newly added client, both times are the same. A successful `edit` changes `Last Modified` even when the entered values match the existing values, while `Created` stays the same. Viewing clients with `list` or `find` does not change either time. Timestamps cannot be edited through commands.

Older saved profiles may lack timestamps. If one time is available, the side panel shows only that time. If both are unavailable, it shows `Timestamp information is unavailable for this profile.` once. Editing an older profile adds a `Last Modified` time but does not invent a `Created` time.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name, phone number or card number: `find`

Finds persons by partial name keywords, full phone numbers, a full card number, or a combination of these.

Format: `find [n/KEYWORD [MORE_KEYWORDS]...] [p/PHONE_NUMBER [MORE_PHONE_NUMBERS]...] [cn/CARD_NUMBER]`

* Supply at least one of `n/`, `p/` or `cn/`. Each prefix may appear only once and must have a nonblank value.
* Use `n/` for names. Bare keywords such as `find Alice` are invalid; use `find n/Alice`.
* Name searches are case-insensitive; for example, `find n/hans` matches `Hans`.
* Name keywords match any contiguous part of a name word; for example, `Han`, `ans` and `ns` all match `Hans`.
* Use `p/` for phone numbers. Each number must contain 3-15 digits and must match the entire stored number,
  including leading zeros. Phone fragments do not match longer numbers.
* Use `cn/` for one complete card number. It must match the stored value exactly, including leading zeros,
  internal spaces and hyphens. Leading and trailing whitespace is ignored. Partial or masked numbers do not match.
  Profiles without recorded card details do not match a card search.
* Separate multiple name keywords or phone numbers with spaces. Within each field, matching any value is enough
  (an `OR` search). For example, `find n/Hans Bo` returns `Hans Gruber` and `Bo Yang`.
* When multiple prefixes are supplied, a person must match every supplied field (an `AND` search).
  For example, `n/` and `p/` together require at least one name keyword **and** at least one phone number to match.
* The prefixes may be in any order, as may name keywords and phone-number alternatives.
* Each prefix searches only its own field. Other fields such as email, address and tags are not searched.

Examples:
* `find n/John` returns persons such as `john` and `John Doe`.
* `find n/lic` returns persons such as `Alice` and `Alicia Tan`.
* `find n/alex david` returns persons such as `Alex Yeoh` and `David Li`.
* `find p/91234567` returns all persons whose phone number is exactly `91234567`.
* `find p/91234567 98765432` returns persons with either phone number.
* `find n/lic p/91234567` returns persons whose names contain `lic` and whose phone number is `91234567`.
* `find cn/4111111111111111` returns all persons with that exact recorded card number.
* `find cn/4111 1111 1111 1111` matches the card number stored with those spaces.
* `find n/lic p/91234567 cn/4111111111111111` returns persons matching all three fields.

### Maintaining a service note: `note`

Each client can have one service note. It appears in the last section of the selected client's details pane, or as `(none)` when no note is saved.

Format: `note r/REFERENCE s/NOTE`

Both prefixes are required, in either order. The reference is case-insensitive. A note can contain up to 200 printable characters on one line; leading and trailing spaces are removed. Setting a note replaces the previous one. Use `s/` with no value to clear it.

Examples:
* `note r/C0001 s/Prefers morning appointments`
* `note s/Hearing impaired, speak clearly r/c0001`
* `note r/C0001 s/`

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find n/Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Removing a client's card: `removecard`

Removes the card summary associated with a client reference while retaining the client's profile and other details. The reference is case-insensitive and is normalized to uppercase. After the change is saved, the client's row refreshes without the card summary and remains selected if it was selected before.

Format: `removecard r/REFERENCE`

Example: `removecard r/C0001`

For a card numbered `4111111111111111`, the success message is `Removed card ending in 1111 from client C0001`. The saved client no longer has a `cardDetails` field.

Possible errors:

* Missing `r/REFERENCE`: `Missing client reference. Format: removecard r/REFERENCE`
* Repeated `r/` parameter: `Parameter was specified more than once.`
* Extra text or an unknown prefix: `Invalid removecard command. Format: removecard r/REFERENCE`
* Unknown reference: `No client found with reference C0001.` (with the requested reference)
* Client without a card: `Client C0001 is not associated with a card.` (with the requested reference)

If saving fails, the app reports `Unable to save client data. No changes were made.` and retains the card in both the list and saved file.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every successful `add`, `edit`, `delete`, `removecard`, or `clear` command. You do not need to save manually. If saving fails, it displays `Unable to save client data. No changes were made.` and keeps the list and saved file unchanged.

On startup, saved clients appear in the main window with `Loaded [COUNT] client(s).`. If there is no data file, sample clients appear with `No saved client data found. Showing sample clients.` The sample clients are saved with the next successful data-changing command. An existing but empty data file starts with an empty list and `No saved client data found.`

### Editing the data file

AddressBook data is saved automatically as a plaintext JSON file `[JAR file location]/data/addressbook.json`. This includes client references and card details. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid or unreadable, including a malformed timestamp value, AddressBook starts with an empty list in protected mode and shows `Unable to load client data. Existing data was not changed. Add, delete, edit, and clear commands are disabled for this session.` All data-changing commands are disabled until you fix the file and restart; the existing file is not overwritten. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/REFERENCE n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS cn/CARD_NUMBER cvv/CVV exp/EXPIRY_DATE provider/PROVIDER cs/CREDIT_SCORE d/DEBT [t/TAG]... ` <br> e.g., `add r/C0001 n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/0.00 t/friend t/colleague`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find [n/KEYWORD [MORE_KEYWORDS]...] [p/PHONE_NUMBER [MORE_PHONE_NUMBERS]...] [cn/CARD_NUMBER]` (at least one prefix required)<br> e.g., `find n/Jam Jak`, `find p/91234567` or `find cn/4111111111111111`
**List**   | `list`
**Remove card** | `removecard r/REFERENCE`<br> e.g., `removecard r/C0001`
**Note**   | `note r/REFERENCE s/NOTE`<br> e.g., `note r/C0001 s/Prefers morning appointments`
**Help**   | `help`
