# Refactoring for Drag-and-Drop Reordering

## Goal Description
Allow users to reorder the feature cards on the main screen by long-pressing and dragging. To achieve this in a standard, scalable way, we will refactor the static `GridLayout` into a `RecyclerView` with `ItemTouchHelper`.

## User Review Required
> [!NOTE]
> This refactor changes the underlying structure of the main screen. The visual appearance will remain largely the same, but the internal logic for handling clicks and layout will be more dynamic.

## Proposed Changes

### App Module
#### [NEW] `FeatureItem.kt`
Data class to represent a dashboard feature (ID, title, icon).

#### [NEW] `FeatureAdapter.kt`
RecyclerView adapter to display feature cards. Triggers click events back to `MainActivity`.

#### [NEW] `item_feature.xml`
Layout file for a single card, extracted from `activity_main.xml`.

#### [MODIFY] `MainActivity.kt`
- Remove individual `CardView` and `TextView` bindings.
- Initialize `RecyclerView` and `FeatureAdapter`.
- Implement `ItemTouchHelper` for drag-and-drop.
- Handle feature clicks via a unified interface.
- **Persistence**: Save the order of feature IDs to `SharedPreferences` in `onPause` or after drag, and load them in `onCreate`.

#### [MODIFY] `activity_main.xml`
- Replace `ScrollView` + `GridLayout` with a single `androidx.recyclerview.widget.RecyclerView`.

## Verification Plan
### Manual Verification
1.  **Drag Test**: Long press a card, drag it to a new position, release. Verify it stays.
2.  **Persistence Test**: Restart the app. Verify the new order is preserved.
3.  **Functionality Test**: Click every card (Flashlight, Dice, etc.) to ensure they still work as expected.
