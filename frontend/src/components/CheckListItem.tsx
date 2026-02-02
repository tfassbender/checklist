import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { CheckListItem as CheckListItemType } from '../types'

interface CheckListItemProps {
  item: CheckListItemType
  editMode: boolean
  onToggle: (id: string, checked: boolean) => void
  onEdit: (id: string) => void
  onDelete: (id: string) => void
}

export default function CheckListItem({
  item,
  editMode,
  onToggle,
  onEdit,
  onDelete,
}: CheckListItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id, disabled: !editMode })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`flex items-center gap-3 p-3 bg-white dark:bg-gray-800 rounded-lg
                  border border-gray-200 dark:border-gray-700
                  ${isDragging ? 'opacity-50 shadow-lg' : ''}
                  ${item.checked ? 'bg-gray-50 dark:bg-gray-800/50' : ''}`}
    >
      {editMode && (
        <button
          {...attributes}
          {...listeners}
          className="touch-none p-1 cursor-grab active:cursor-grabbing
                     text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          title="Drag to reorder"
        >
          <svg
            className="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="8" y1="6" x2="8" y2="6" />
            <line x1="8" y1="12" x2="8" y2="12" />
            <line x1="8" y1="18" x2="8" y2="18" />
            <line x1="16" y1="6" x2="16" y2="6" />
            <line x1="16" y1="12" x2="16" y2="12" />
            <line x1="16" y1="18" x2="16" y2="18" />
          </svg>
        </button>
      )}

      <label className="flex items-center gap-3 flex-1 min-h-touch cursor-pointer">
        <input
          type="checkbox"
          checked={item.checked}
          onChange={e => onToggle(item.id, e.target.checked)}
          className="w-5 h-5 rounded border-gray-300 dark:border-gray-600
                     text-primary-600 focus:ring-primary-500
                     dark:bg-gray-700 cursor-pointer"
        />
        <span
          className={`flex-1 ${
            item.checked
              ? 'text-gray-500 dark:text-gray-400 line-through'
              : 'text-gray-900 dark:text-gray-100'
          }`}
        >
          {item.description}
        </span>
      </label>

      {editMode && (
        <div className="flex items-center gap-1">
          <button
            onClick={() => onEdit(item.id)}
            className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700
                       text-gray-600 dark:text-gray-400"
            title="Edit item"
          >
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
          </button>
          <button
            onClick={() => onDelete(item.id)}
            className="p-2 rounded-lg hover:bg-red-100 dark:hover:bg-red-900/30
                       text-red-600 dark:text-red-400"
            title="Delete item"
          >
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
        </div>
      )}
    </div>
  )
}
