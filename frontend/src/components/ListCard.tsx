import { Link } from 'react-router-dom'
import { CheckListSummary } from '../types'

interface ListCardProps {
  list: CheckListSummary
  onDelete?: (id: string) => void
}

export default function ListCard({ list, onDelete }: ListCardProps) {
  const isComplete = list.itemCount > 0 && list.checkedCount === list.itemCount
  const progressPercent =
    list.itemCount > 0 ? (list.checkedCount / list.itemCount) * 100 : 0

  return (
    <div className="card p-4 relative group">
      <Link to={`/list/${list.id}`} className="block">
        <div className="flex items-start justify-between mb-2">
          <h3 className="font-medium text-lg truncate pr-2">{list.name}</h3>
          {isComplete && (
            <span className="flex-shrink-0 text-green-500">
              <svg
                className="w-5 h-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </span>
          )}
        </div>

        <div className="text-sm text-gray-600 dark:text-gray-400 mb-3">
          {list.itemCount === 0
            ? 'No items'
            : `${list.checkedCount}/${list.itemCount} done`}
        </div>

        {list.itemCount > 0 && (
          <div className="h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ${
                isComplete ? 'bg-green-500' : 'bg-primary-500'
              }`}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        )}
      </Link>

      {onDelete && (
        <button
          onClick={e => {
            e.preventDefault()
            e.stopPropagation()
            onDelete(list.id)
          }}
          className="absolute top-2 right-2 p-2 rounded-lg opacity-0 group-hover:opacity-100
                     hover:bg-red-100 dark:hover:bg-red-900/30 text-red-600 dark:text-red-400
                     transition-opacity focus:opacity-100"
          title="Delete list"
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
      )}
    </div>
  )
}
