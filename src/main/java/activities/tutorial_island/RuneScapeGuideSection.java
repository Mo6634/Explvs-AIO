package activities.tutorial_island;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.script.MethodProvider;
import util.Sleep;
import util.event.DisableAudioEvent;
import util.event.EnableFixedModeEvent;
import util.event.ToggleRoofsHiddenEvent;
import util.event.ToggleShiftDropEvent;
import util.widget.CachedWidget;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public final class RuneScapeGuideSection extends TutorialSection {


    private final CachedWidget nameAcceptedWidget = new CachedWidget(w -> w.getMessage().contains("Great!"));

    private final CachedWidget nameLookupWidget = new CachedWidget(w -> w.getMessage().contains("Look up name"));
    private final CachedWidget nameInputWidget = new CachedWidget(w -> w.getMessage().contains("What name would you like to check"));
    private final CachedWidget nameSetWidget = new CachedWidget("Set name");
    private final CachedWidget nameScreenDetectionWidget = new CachedWidget("Choose display name");

    private final CachedWidget creationScreenWidget = new CachedWidget("Head");
    private final CachedWidget experienceWidget = new CachedWidget("What's your experience with Old School Runescape?");
    private boolean isAudioDisabled;

    public RuneScapeGuideSection() {
        super("Gielinor Guide");
    }

    @Override
    public final void onLoop() throws InterruptedException {
        if (pendingContinue()) {
            selectContinue();
            return;
        }

        switch (getProgress()) {
            case 0:
            case 1:
            case 2:
                if (nameScreenDetectionWidget.get(getWidgets()).isPresent()) {
                    setDisplayName();
                } else if (isCreationScreenVisible()) {
                    createRandomCharacter();
                } else if (experienceWidget.get(getWidgets()).isPresent()) {
                    if (getDialogues().selectOption(random(1, 3))) {
                        Sleep.sleepUntil(() -> !experienceWidget.get(getWidgets()).map(widget -> !widget.isVisible()).orElse(true), 2000, 600);
                    }
                } else {
                    talkToInstructor();
                }
                break;
            case 3:
                getTabs().open(Tab.SETTINGS);
                break;
            case 10:
                if (!EnableFixedModeEvent.isFixedModeEnabled(this)) {
                    execute(new EnableFixedModeEvent());
                } else if (!isAudioDisabled) {
                    isAudioDisabled = disableAudio();
                } else if (!getSettings().areRoofsEnabled()) {
                    toggleRoofsHidden();
                } else if (!getSettings().isShiftDropActive()) {
                    toggleShiftDrop();
                } else if (getObjects().closest("Door").interact("Open")) {
                    Sleep.sleepUntil(() -> getProgress() != 10, 5000, 600);
                }
                break;
            default:
                talkToInstructor();
                break;
        }
    }

    private void setDisplayName() {
        if (nameAcceptedWidget.get(getWidgets()).isPresent()) {
            nameSetWidget.get(getWidgets()).ifPresent(rs2Widget -> {
                if (rs2Widget.interact()) {
                    Sleep.sleepUntil(() -> !nameScreenDetectionWidget.get(getWidgets()).isPresent(), 8000, 600);
                }
            });
        } else if (nameInputWidget.get(getWidgets()).isPresent()
                && nameInputWidget.get(getWidgets()).get().isVisible()
                && getKeyboard().typeString(generateRandomString(5), true)) {

            final int configValue = getConfigs().get(1042);

            Sleep.sleepUntil(() -> getConfigs().get(1042) != configValue, 8000, 600);
            Sleep.sleepUntil(() -> getConfigs().get(1042) == configValue || nameAcceptedWidget.get(getWidgets()).isPresent(), 8000, 600);
        } else if (nameLookupWidget.get(getWidgets()).isPresent()
                && nameLookupWidget.get(getWidgets()).get().interact()) {
            Sleep.sleepUntil(() -> nameInputWidget.get(getWidgets()).isPresent() && nameInputWidget.get(getWidgets()).get().isVisible(), 8000, 600);
        }
    }

    private String generateRandomString(int maxLength) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";
        return new Random().ints(new Random().nextInt(maxLength) + 1, 0, chars.length())
                .mapToObj(i -> "" + chars.charAt(i))
                .collect(Collectors.joining());
    }

    private boolean isCreationScreenVisible() {
        return creationScreenWidget.get(getWidgets()).filter(RS2Widget::isVisible).isPresent();
    }

    private void createRandomCharacter() throws InterruptedException {
        if (new Random().nextInt(2) == 1) {
            getWidgets().getWidgetContainingText("Female").interact();
        }

        final RS2Widget[] childWidgets = getWidgets().getWidgets(creationScreenWidget.get(getWidgets()).get().getRootId());
        Collections.shuffle(Arrays.asList(childWidgets));

        for (final RS2Widget childWidget : childWidgets) {
            if (childWidget.getToolTip() == null) {
                continue;
            }
            if (childWidget.getToolTip().contains("Change") || childWidget.getToolTip().contains("Recolour")) {
                clickRandomTimes(childWidget);
            }
        }

        if (getWidgets().getWidgetContainingText("Accept").interact()) {
            Sleep.sleepUntil(() -> !isCreationScreenVisible(), 3000, 600);
        }
    }

    private void clickRandomTimes(final RS2Widget widget) throws InterruptedException {
        int clickCount = new Random().nextInt(4);

        for (int i = 0; i < clickCount; i++) {
            if (widget.interact()) {
                MethodProvider.sleep(150);
            }
        }
    }

    private boolean disableAudio() {
        Event disableAudioEvent = new DisableAudioEvent();
        execute(disableAudioEvent);
        return disableAudioEvent.hasFinished();
    }

    private boolean toggleRoofsHidden() {
        Event toggleRoofsHiddenEvent = new ToggleRoofsHiddenEvent();
        execute(toggleRoofsHiddenEvent);
        return toggleRoofsHiddenEvent.hasFinished();
    }

    private boolean toggleShiftDrop() {
        Event toggleShiftDrop = new ToggleShiftDropEvent();
        execute(toggleShiftDrop);
        return toggleShiftDrop.hasFinished();
    }
}